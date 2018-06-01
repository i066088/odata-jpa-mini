insert into FOO (NAME, ADDRESS, AGE, BIRTHDAY, HEIGHT)values('Mickey', 'Clark Street 33',45.8,null,160.0);
insert into FOO (NAME, ADDRESS, AGE, BIRTHDAY, HEIGHT)values('Goofy', 'Clark Street 35',40.8,null,190.0);
insert into BAR (DESCRIPTION)values('Liverpool');
insert into BAR (DESCRIPTION)values('Madrid');
insert into BAR (DESCRIPTION)values('Roma');
insert into FOO_BAR (FOO_ID, BARS_ID) select f.ID, b.ID from FOO f, BAR b where f.name='Mickey' and b.description in('Liverpool','Roma');