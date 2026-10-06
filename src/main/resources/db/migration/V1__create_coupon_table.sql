CREATE TABLE coupon (
    id              UUID                     NOT NULL PRIMARY KEY,
    code            VARCHAR(6)               NOT NULL,
    description     VARCHAR                  NOT NULL,
    discount_value  NUMERIC(19, 4)           NOT NULL,
    expiration_date TIMESTAMP WITH TIME ZONE NOT NULL,
    published       BOOLEAN                  NOT NULL,
    redeemed        BOOLEAN                  NOT NULL,
    deleted_at      TIMESTAMP WITH TIME ZONE,
    version         BIGINT                   NOT NULL
);
