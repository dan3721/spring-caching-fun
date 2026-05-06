# Pokemon Spring Caching Demo

Spring Boot demo app that uses:
- Postgres for set storage
- Redis for cache storage
- Spring Cache (`@Cacheable`) in `PokemonService#getSets(String series)`

## Prereqs

- Java 21
- Maven 3.9+
- Docker + Docker Compose

## Start infrastructure

```bash
docker compose up -d
```

This starts:
- Postgres on `localhost:15432` (`pokemon` / `pokemon`)
- Redis on `localhost:16379`

These are intentionally non-default host ports so this compose file can run alongside other local Postgres/Redis instances that commonly bind `5432` / `6379`.

On first Postgres startup, `docker/postgres/init/01-init.sql`:
- creates table `pokemon_set`
- loads `set-data.json` into the table

Set data in `set-data.json` was taken from the Pokémon TCG community dataset: [PokemonTCG/pokemon-tcg-data `sets/en.json`](https://github.com/PokemonTCG/pokemon-tcg-data/blob/master/sets/en.json) (English sets list).

If you already had a local Postgres volume from an older version of this demo (for example table `pokemon_sets` instead of `pokemon_set`), **restarting containers is not enough** — init scripts under `/docker-entrypoint-initdb.d` only run when the data directory is empty. Either **recreate the volumes** (below) or migrate the schema manually (e.g. `ALTER TABLE`).

To **delete the database (and Redis) data stored in this project’s named volumes** and run init from scratch:

```bash
docker compose down -v
docker compose up -d
```

The **`-v` flag removes volumes** declared in `docker-compose.yml` (here `postgres-data` and `redis-data`), so Postgres data under `/var/lib/postgresql/data` and Redis data are wiped. Do not use `-v` if you need to keep that data.

## Run app

```bash
./mvnw spring-boot:run
```

This project includes `spring-boot-devtools`, which restarts the app when the **compiled classpath** changes. In IntelliJ that usually means **Build → Build Project** (or enable automatic builds on save); otherwise edits won’t trigger a restart until something recompiles.

## API

Fetch sets by series:

```bash
curl "http://localhost:8080/api/pokemon/sets?series=Base"
```

Create a new set:

```bash
curl -X POST "http://localhost:8080/api/pokemon/sets" \
  -H "Content-Type: application/json" \
  -d '{"id":"demo-set-1","name":"Demo Set","series":"Demo","printedTotal":10,"total":12,"releaseDate":"2026-05-06","updatedAt":"2026-05-06T09:00:00","symbolUrl":"https://example.com/symbol.png","logoUrl":"https://example.com/logo.png"}'
```

Fetch all available series names:

```bash
curl "http://localhost:8080/api/pokemon/series"
```

Same data with **distributed single-flight** (`@CacheableD` + Spring Integration `RedisLockRegistry`, cache `pokemonSeriesNamesD`, 5-minute lock lease and cache TTL):

```bash
curl "http://localhost:8080/api/pokemon/series-d"
```

Call the same request twice and compare response time:
- first call should hit Postgres
- second call should be served from Redis cache (set `logging.level.org.springframework.cache` to `DEBUG` or `TRACE` in `application.yml` if you want cache hit/miss logs; defaults are `WARN`)

If you ever hit a Redis cache deserialization error after changing what gets cached (types/fields) or the cache serializer, clear old cache entries:

```bash
docker exec -it pokemon-redis redis-cli FLUSHALL
```

This project also versions the Redis cache key prefix in `application.yml` (`spring.cache.redis.key-prefix`) so incompatible cached entries can be abandoned without wiping Redis.

Note: cache **values** are stored as **JSON** (via `GenericJackson2JsonRedisSerializer`, including Jackson `@class` type hints for lists).

`spring.cache.redis.key-prefix` is read from `application.yml` and applied in `CacheConfig` via `RedisCacheConfiguration#prefixCacheNameWith` (a `RedisCacheManagerBuilderCustomizer` that calls `cacheDefaults(...)` must **repeat** any Boot defaults it replaces—otherwise the prefix from YAML is ignored and keys stay `cacheName::key` only).

## Project structure

- `src/main/java/com/example/pokemon/set/PokemonService.java` - cached service
- `src/main/java/com/example/pokemon/jpa/PokemonSetEntity.java` - JPA entity
- `src/main/java/com/example/pokemon/jpa/PokemonSetRepository.java` - Spring Data JPA
- `src/main/java/com/example/pokemon/set/PokemonController.java` - REST endpoint
- `docker-compose.yml` - Postgres + Redis
- `docker/postgres/init/01-init.sql` - schema + JSON data load

## Redis CMDs to Check the Cahed Data

```bash
docker exec -it pokemon-redis redis-cli --scan --pattern '*pokemonSetsBySeries*'

docker exec -it pokemon-redis redis-cli GET 'pokemon-caching:v6:pokemonSetsBySeries::base'
```