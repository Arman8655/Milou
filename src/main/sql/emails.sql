use email_project;

create table emails (
    id int auto_increment primary key ,
    sender_id int not null ,
    subject nvarchar(255) not null ,
    body text not null ,
    timestamp date not null ,
    email_code nvarchar(6) not null unique ,

    foreign key (sender_id) references users(id)
);