create table submit_meter_reads
(
    id                             bigserial
                                             not null
        primary key,
    chat_id                        bigserial
                                             not null
        unique,
    datetime_of_submit_meter_reads timestamp not null
);
