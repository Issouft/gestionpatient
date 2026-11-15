-- V4 : le super-administrateur, qui gère la plateforme (les établissements et leurs administrateurs).

-- 1. Le nouveau rôle est accepté pour un compte.
alter table utilisateur drop constraint utilisateur_role_check;
alter table utilisateur add constraint utilisateur_role_check
    check (role in ('MEDECIN','SECRETAIRE','PATIENT','PHARMACIEN','ADMINISTRATEUR','SUPER_ADMINISTRATEUR'));

-- 2. Sa table, liée à utilisateur par le même id (héritage JOINED, comme les autres rôles).
create table super_administrateur (
    id integer not null,
    primary key (id),
    constraint fk_super_administrateur_utilisateur foreign key (id) references utilisateur
);

-- La table affiliation n'accepte toujours pas SUPER_ADMINISTRATEUR (voir V3) : c'est voulu.
-- Le super-administrateur n'appartient à aucun établissement, la base l'interdit même si le code se trompait.
