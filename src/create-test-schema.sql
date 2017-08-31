  
CREATE TABLE jsoncrud_sample_users
(
    id bigserial NOT NULL,
    uid character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    gender "char",
    age integer,
    enabled boolean DEFAULT false,
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP),
    CONSTRAINT sample_users_pkey PRIMARY KEY (id),
    CONSTRAINT sample_users_uin_uc UNIQUE (uid)
);

CREATE TABLE jsoncrud_sample_user_attrs
(
    id bigserial NOT NULL,
    user_id bigint NOT NULL REFERENCES jsoncrud_sample_users(id),
    attrkey character varying(20) NOT NULL,
    attrval character varying(500) NOT NULL,
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP),
    CONSTRAINT sample_user_attrs_pkey PRIMARY KEY (id),
    CONSTRAINT sample_user_attrs_uc UNIQUE (user_id, attrkey)
);

CREATE TABLE jsoncrud_sample_roles
(
    id bigserial NOT NULL,
    role_name character varying NOT NULL,
    role_desc character varying,
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP),
    CONSTRAINT sample_roles_pkey PRIMARY KEY (id)
);

CREATE TABLE jsoncrud_sample_userroles
(
    id bigserial NOT NULL,
    role_id bigint NOT NULL REFERENCES jsoncrud_sample_roles(id),
    user_id bigint NOT NULL REFERENCES jsoncrud_sample_users(id),
    created_timestamp bigint DEFAULT extract('epoch' from CURRENT_TIMESTAMP),
    CONSTRAINT sample_userroles_pkey PRIMARY KEY (id),
    CONSTRAINT sample_userrole_uc UNIQUE (role_id, user_id)
);