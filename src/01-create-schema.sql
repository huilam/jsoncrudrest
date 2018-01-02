
CREATE TABLE jsoncrud_cfg
(
    cfg_id bigserial NOT NULL,
    cfg_app_namespace character varying(256) NOT NULL,
    cfg_module_code character varying(128) NOT NULL,
    enabled boolean DEFAULT true,
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP) * 1000,
    CONSTRAINT jsoncrud_cfg_pkey PRIMARY KEY (cfg_id),
    CONSTRAINT uc_jsoncrud_cfg UNIQUE (cfg_app_namespace, cfg_module_code)
);

CREATE TABLE jsoncrud_cfg_values
(
    cfg_id bigint NOT NULL REFERENCES jsoncrud_cfg(cfg_id),
    cfg_key character varying(100) NOT NULL,
    cfg_value character varying(512) NOT NULL,
    enabled boolean DEFAULT true,
    cfg_seq integer DEFAULT 0,
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP) * 1000,
    CONSTRAINT jsoncrud_cfg_values_pkey PRIMARY KEY (cfg_id, cfg_key)
);

