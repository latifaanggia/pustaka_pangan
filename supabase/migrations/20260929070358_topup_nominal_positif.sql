-- Tolak top up dengan nominal 0/negatif di level database (lapis kedua setelah validasi di aplikasi).
-- NOT VALID: data lama (mis. data uji Rp0 dari regression testing 29/09) tidak dicek, tapi semua insert/update baru wajib lolos.
alter table public.topup add constraint topup_nominal_positif check (nominal > 0) not valid;