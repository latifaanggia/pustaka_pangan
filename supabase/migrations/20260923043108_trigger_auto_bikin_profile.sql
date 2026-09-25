-- Fungsi yang bakal dijalanin otomatis
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, nama_depan, nama_belakang, saldo)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'nama_depan', ''),
    coalesce(new.raw_user_meta_data->>'nama_belakang', ''),
    0
  );
  return new;
end;
$$ language plpgsql security definer set search_path = public;

-- Trigger: begitu ada baris baru masuk ke auth.users (user baru daftar), panggil fungsi di atas
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
