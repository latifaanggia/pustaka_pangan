-- Nyalain RLS di semua tabel
alter table public.profiles enable row level security;
alter table public.majalah enable row level security;
alter table public.topup enable row level security;
alter table public.langganan enable row level security;

-- profiles: user cuma bisa baca & edit baris miliknya sendiri
create policy "User bisa lihat profil sendiri" on public.profiles
  for select using (auth.uid() = id);
create policy "User bisa update profil sendiri" on public.profiles
  for update using (auth.uid() = id);
create policy "User bisa bikin profil sendiri saat daftar" on public.profiles
  for insert with check (auth.uid() = id);

-- majalah: semua orang (termasuk yang belum login) boleh baca, tidak ada yang boleh tulis dari app
create policy "Semua orang boleh baca daftar majalah" on public.majalah
  for select using (true);

-- topup: user cuma bisa lihat & bikin baris top up miliknya sendiri
create policy "User bisa lihat top up sendiri" on public.topup
  for select using (auth.uid() = customer_id);
create policy "User bisa bikin top up sendiri" on public.topup
  for insert with check (auth.uid() = customer_id);

-- langganan: user cuma bisa lihat langganan miliknya sendiri
create policy "User bisa lihat langganan sendiri" on public.langganan
  for select using (auth.uid() = customer_id);
