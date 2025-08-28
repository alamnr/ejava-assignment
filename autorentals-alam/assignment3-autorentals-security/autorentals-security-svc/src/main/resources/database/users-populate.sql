--users-populate.sql

--insert into users(username, password, enabled) values ('mary','{noop}secret', true);
insert into users(username, password, enabled) values ('mary', '{bcrypt}$2y$10$srOP.5nN.86dTt5bCmpaf.XbAlwcVVQp.M/tA0Bg7T4XexSKvjd8G', true);

--insert into users(username, password, enabled) values ('lou','{noop}secret', true);
insert into users(username, password, enabled) values ('lou', '{bcrypt}$2y$10$ACy4EXCzR0BLGeLHCZwvwOyFccB34CDWRAuQCpHiqbh86se2W9906', true);

--insert into users(username, password, enabled) values ('murray','{noop}secret', true);
insert into users(username, password, enabled) values ('murray', '{bcrypt}$2y$10$VlmqQcEXDm1UojW3IHa9au.u4Id2g2oQxMUpqrCNH.oWWAndzI2te', true);

--insert into users(username, password, enabled) values ('ted','{noop}secret', true);
insert into users(username, password, enabled) values ('ted', '{bcrypt}$2y$10$GfB9Oxxi/tStkMuQ05sq7e2ANvo4f3zZ8ER70zWNGree8LL.LqOC6', true);

--insert into users(username, password, enabled) values ('sueann','{noop}betty', true);
insert into users(username, password, enabled) values ('sueann', '{bcrypt}$2y$10$NUOCifN1pxiLBJa6kTQLSOVRRIgufEkBJreOU6HE45KEv1RRrZ2Rm', true);


insert into authorities(username,authority) values('mary','known');
insert into authorities(username,authority) values('lou','known');
insert into authorities(username,authority) values('murray','known');
insert into authorities(username,authority) values('ted','known');
insert into authorities(username,authority) values('sueann','known');