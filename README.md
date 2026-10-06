# Coupon API

Desafio técnico - Onebrain

API de cadastro e inativação de cupons

## Stack

Java 21 · Spring Boot 4 · Spring Data JPA · H2 (em memória) · Flyway · springdoc-openapi · JUnit 5 · AssertJ · ArchUnit · JaCoCo · Docker

## Para rodar a aplicação:

### Docker Compose

```bash
docker compose up --build
```

### Local

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```


### Acessos

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

### H2

- H2 console (profile `local`): http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:coupondb`, usuário `sa`)

## Testes

```bash
./gradlew check
```

Roda os testes, as condições de desenvolvimento e teste de cobertura (80%). Relatório ficará disponível no `build/reports/jacoco/test/html/index.html`.

## Arquitetura

Hexagonal

```
com.loris.onebrain.coupon
├── domain           regras de negócio
├── application      use cases (CreateCoupon, GetCoupon, DeleteCoupon) e port CouponRepository
└── infrastructure
    ├── config       wiring Spring
    ├── persistence  adapter JPA + Flyway
    └── web          controller, DTOs, ProblemDetail, OpenAPI
```

## Regras de negócio

|  | Regra |  |
|---|---|---|
| 01 | `code`, `description`, `discountValue` e `expirationDate` devem ser obrigatórios. | VOs / `Coupon.create` |
| 02 | O `code` aceita caracteres especiais na entrada, mas devem ser removidos antes do save e do response. | `CouponCode` |
| 03 | Depois da sanitização, o `code` devem ter **apenas 6** caracteres alfanuméricos (`A-Z`, `0-9`). | `CouponCode` |
| 04 | Caracteres com acentos devem ser convertidos para o formato sem acento (`É` → `E`). Letras sem decomposição (`ß`, `Æ`, `ø`) e qualquer outro caractere especial devem ser removidos. | `CouponCode` |
| 05 | O `code` deve ser normalizado para maiúsculas. | `CouponCode` |
| 06 | `discountValue` ≥ **0.5**, sem valor máximo. | `DiscountValue` |
| 07 | `expirationDate` nunca deve ser menor que o momento da criação. | `Coupon.create` |
| 08 | A publicação do cupom deve ser opcional (`published = true`). Por default `published = false`. | `Coupon.create` |
| 09 | Todo cupom deverá ser criado com status `ACTIVE` e `redeemed = false`. | `Coupon.create` |
| 10 | Um cupom poderá ser deletado a qualquer momento | `Coupon.delete` |
| 11 | A exclusão deve ser um **soft delete**, registrando um `deletedAt` e preservando todos os dados recebidos no cadastro. | `Coupon.delete` / persistência |
| 12 | Não deve ser possível deletar um cupom já deletado. | `Coupon.delete` |
| 13 | Um cupom deletado continua consultável, com status `DELETED`. | `Coupon.status` / `GET` |
| 14 | A `description` deverá ser obrigatória e deverá ter um trim para evitar espaços no início e no fim | `CouponDescription` |
