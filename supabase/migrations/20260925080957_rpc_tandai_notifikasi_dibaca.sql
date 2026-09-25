-- Tandai notifikasi dibaca. p_id null = tandai semua. SECURITY INVOKER: tetap tunduk ke RLS + grant kolom sudah_dibaca
-- (dibuat sebagai RPC karena HttpURLConnection di Android tidak mendukung method PATCH)
create or replace function public.tandai_notifikasi_dibaca(p_id integer default null)
returns integer language sql security invoker set search_path = '' as $$
  with diubah as (
    update public.notifikasi set sudah_dibaca = true
    where customer_id = auth.uid() and sudah_dibaca = false and (p_id is null or id = p_id)
    returning 1
  ) select count(*)::integer from diubah
$$;
revoke execute on function public.tandai_notifikasi_dibaca(integer) from public, anon;
grant execute on function public.tandai_notifikasi_dibaca(integer) to authenticated;
