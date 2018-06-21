insert into FOO (NAME, ADDRESS, AGE, BIRTHDAY, HEIGHT)values('Mickey', 'Clark Street 33',45.8,null,160.0);
insert into FOO (NAME, ADDRESS, AGE, BIRTHDAY, FULL_BIRTH_HOUR, HEIGHT)values('Goofy', 'Clark Street 36',40.8,'1982-03-27','1982-03-27 04:32:27',190.0);
insert into FOO (NAME, ADDRESS, AGE, BIRTHDAY,  HEIGHT)values('Daisy', 'Clark Street 37',38.8,'1986-03-27',160.0);
insert into FOO (NAME, ADDRESS, AGE, HEIGHT)values('Minnie', 'Clark Road 1',39,165.0);
insert into BAR (DESCRIPTION)values('Liverpool');
insert into BAR (DESCRIPTION)values('Madrid');
insert into BAR (DESCRIPTION)values('Roma');
insert into FOO_BAR (FOO_ID, BARS_ID) select f.ID, b.ID from FOO f, BAR b where f.name='Mickey' and b.description in('Liverpool','Roma');
update FOO set FAVOURITE_BAR_ID=(select ID from BAR where DESCRIPTION='Roma') where NAME='Mickey';
