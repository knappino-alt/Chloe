# Chloe ⭐

Android appka pro tablet – obal kolem rodinné appky **Domácnost Knappovi** (Claude).
Otevře dětský režim (Alice a Dominik, úkoly, hvězdičky do pokladny) přes celou obrazovku.

## Kde stáhnout
Sekce **Releases** vpravo → nejnovější verze → `Chloe.apk`.

## Jak to funguje
- Při každé změně v repozitáři GitHub sám sestaví novou `Chloe.apk` (záložka **Actions**).
- Adresa appky je v `app/src/main/res/values/strings.xml` (`launch_url`).
- Appka se otevírá přes Chrome (Trusted Web Activity) – přihlášení do Clauda funguje stejně jako v Chromu.
- Ven z dětského režimu: podržet nápis „rodiče“ 2 vteřiny.
