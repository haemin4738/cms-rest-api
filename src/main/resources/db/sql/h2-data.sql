-- 기본 계정 삽입
-- admin / admin1234
insert into users (username, password, role)
values ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN');

-- user1 / user1234
insert into users (username, password, role)
values ('user1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO1S0W.xFwC', 'USER');