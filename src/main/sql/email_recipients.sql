create table email_recipients (
    id int auto_increment primary key ,
    email_id int not null ,
    receiver_id int not null ,
    is_read boolean not null  default false,


    foreign key (email_id) references emails(id),
    foreign key (receiver_id) references users(id)
);