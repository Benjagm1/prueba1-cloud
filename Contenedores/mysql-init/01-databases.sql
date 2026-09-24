SELECT 'CREATE DATABASE db_usuarios' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_usuarios')\gexec
SELECT 'CREATE DATABASE db_administracion' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_administracion')\gexec
SELECT 'CREATE DATABASE db_academico' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_academico')\gexec
SELECT 'CREATE DATABASE db_asistencia' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_asistencia')\gexec
SELECT 'CREATE DATABASE db_conducta' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_conducta')\gexec
SELECT 'CREATE DATABASE db_mensajeria' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'db_mensajeria')\gexec
