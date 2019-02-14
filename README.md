# Vezetést segítő funkciók fejlesztése okostelefonra mély tanulás alapon

## TDK dolgozat elérhetősége: [Dolgozat](http://tdk.bme.hu/VIK/Neural/Vezetest-segito-funkciok-fejlesztese)
 Budapesti Műszaki és Gazdaságtudományi Egyetem Tudományos Diákköri Konferencián Neurális Hálózatok szekcióban II. helyezet.

## Rövidebb összefoglaló Medium cikk elérhetősége: [Cikk](http://tdk.bme.hu/VIK/Neural/Vezetest-segito-funkciok-fejlesztese)

## Tartalom

- Általános leírás
- Architektúra ábra
- Modul leírások
- Szoftverek, csomagok
- Futtatás lokálisan 
- Összefoglalás

## Általános leírás
A repository tartalmaz egy Android alkalmazást, amely autókat, buszok és jelző lámpákat detektál a kamera képén úgy, hogy a háttérben mély neurális hálózatok számításait a készülék processzorán futtatja. Az alkalmazás emellett képes videó rögzítésére, amelyet egy FTP tárhelyre fel tud tölteni. A szerver oldal a feltöltött videók feldolgozásával a mély neurális hálózat tovább tanítására képes, majd az „okosabb” hálózatot vissza tudja szinkronizálni az Androidos készülékre. A felhasználók számának növekedésével a begyűjtött tanító adat mennyisége is lineárisan nő, ezzel biztosítva a lehetőséget, hogy olyan adatokkal is tanítsunk, ami egyébként nem állna rendelkezésünkre. Az alkalmazás felhasználók kezelésre is képes, így a felhasználók hozzáférést az alkalmazás funkcióihoz távolról lehet korlátozni.

## Architektúra ábra
![arcitektura](pics/deployment.png)

## Modul leírások
## Szoftverek, csomagok
## Futtatás lokálisan 
## Összefoglalás
