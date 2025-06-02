-- Crear la tabla media_posts
create table if not exists media_posts (
    id uuid default uuid_generate_v4() primary key,
    user_id uuid references auth.users(id) not null,
    caption text,
    media_urls text[] not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Crear índices para mejorar el rendimiento
create index if not exists media_posts_user_id_idx on media_posts(user_id);
create index if not exists media_posts_created_at_idx on media_posts(created_at desc);

-- Configurar RLS (Row Level Security)
alter table media_posts enable row level security;

-- Políticas de seguridad
create policy "Usuarios pueden ver todos los posts"
    on media_posts for select
    using (true);

create policy "Usuarios pueden crear sus propios posts"
    on media_posts for insert
    with check (auth.uid() = user_id);

create policy "Usuarios pueden actualizar sus propios posts"
    on media_posts for update
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

create policy "Usuarios pueden eliminar sus propios posts"
    on media_posts for delete
    using (auth.uid() = user_id);

-- Trigger para actualizar updated_at
create or replace function update_updated_at_column()
returns trigger as $$
begin
    new.updated_at = timezone('utc'::text, now());
    return new;
end;
$$ language plpgsql;

create trigger update_media_posts_updated_at
    before update on media_posts
    for each row
    execute function update_updated_at_column();

-- Comentarios de la tabla
comment on table media_posts is 'Tabla para almacenar posts con contenido multimedia';
comment on column media_posts.id is 'Identificador único del post';
comment on column media_posts.user_id is 'ID del usuario que creó el post';
comment on column media_posts.caption is 'Texto descriptivo del post';
comment on column media_posts.media_urls is 'Array de URLs de los archivos multimedia';
comment on column media_posts.created_at is 'Fecha y hora de creación';
comment on column media_posts.updated_at is 'Fecha y hora de última actualización'; 