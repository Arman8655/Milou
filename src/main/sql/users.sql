use email_project;


create table users(
   id int primary key auto_increment,
    name nvarchar(100) not null,
    email nvarchar(100) not null unique,
    password nvarchar(200) not null

);