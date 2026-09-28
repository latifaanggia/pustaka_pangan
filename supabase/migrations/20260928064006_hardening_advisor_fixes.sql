-- Perbaikan hasil audit Supabase Advisors (28 Sep 2026)
-- 1. handle_new_user hanya untuk trigger, bukan untuk dipanggil lewat API
revoke execute on function public.handle_new_user() from public, anon, authenticated;

-- 2. auth.uid() dibungkus (select ...)
alter policy "User bisa lihat profil sendiri" on public.profiles using ((select auth.uid()) = id);
alter policy "User bisa update profil sendiri" on public.profiles using ((select auth.uid()) = id);
alter policy "User bisa lihat top up sendiri" on public.topup using ((select auth.uid()) = customer_id);
alter policy "User bisa bikin top up sendiri" on public.topup with check ((select auth.uid()) = customer_id and status = 'Menunggu Konfirmasi');
alter policy "User bisa lihat langganan sendiri" on public.langganan using ((select auth.uid()) = customer_id);
alter policy "User bisa lihat pembelian sendiri" on public.pembelian using ((select auth.uid()) = customer_id);
alter policy "User bisa lihat notifikasi sendiri" on public.notifikasi using ((select auth.uid()) = customer_id);
alter policy "User bisa tandai notifikasi sendiri dibaca" on public.notifikasi using ((select auth.uid()) = customer_id);

-- 3. Index untuk foreign key
create index if not exists topup_customer_id_idx on public.topup (customer_id);
create index if not exists pembelian_majalah_id_idx on public.pembelian (majalah_id);
create index if not exists langganan_customer_id_idx on public.langganan (customer_id);
create index if not exists langganan_topup_id_idx on public.langganan (topup_id);
