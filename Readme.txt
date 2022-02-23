Resource Booking System - Małgorzata Stocka s23126

1. OPIS IMPLEMENTACJI
-----------------------------------------------

Sposób organizacji sieci
Wezeł posiada parametry przekazywane w  poleceniu: 
- ident (int) 								 identyfikator węzła
- gateway (String)							 ip i port gateway'a, niewymagane przy uruchomieniu pierwszego węzła
- tcpport (int)								 nr portu węzła
							
Wewnętrzna struktura danych w węźle:
- klasa ResourceStorage                      przechowuje informacje o dostępnych zasobach w obrębie węzła oraz o tym które zasoby, w jakiej ilości zostały przez kogo zarezerwowane. Dodatkowo sprawdza, czy dany zasób można zarezerwować w wymaganej ilości
- nodesList (ArrayList<String>)				 lista zawierająca aktualne adresy ip i porty innych węzłów w sieci (zapisane jako String <ip>:<port>)

Każdy węzeł wie o swoich zasobach, jednak nie wie o zasobach innych węzłów. Posiada listę adresów ip i portów i może kolejno wysyłać zapytanie do węzła znajdującego się w sieci, poczekać na odpowiedź. Jeśli odpytywany węzeł nie posiada zasobów, może wysłać zapytanie do kolejnego węzła z listy. 

Każdy węzeł jest infomowany o dodaniu nowego węzła, węzeł który jest określony jako gateway przy uruchamianiu nowego węzła, wysyła do wszystkich węzłów w sieci informację o nr ip i portu nowego węzła (broadcast). Każdy węzeł aktualizuje swoją listę węzłów.

Opis komunikacji i przesyłanych komunikatów

Komendy, które są wykorzystywane do komunikacji pomiędzy węzłami w sieci:

- "TERMINATE"			wyłączanie całej sieci
- "TERMINATE_NODE"      komenda wysyłana do pozostałych węzłów sieci przez węzeł, który otrzyma "TERMINATE" od klienta
- "NEW_NODE <port>"	    nowy węzeł wysyła żądanie dodania do sieci wraz z portem na którym nasłuchuje
- "UPDATE_NODE_LIST <ip>:<port>"	węzeł rozsyła ip i port nowego węzła do wszystkich istniejących węzłów w sieci

Komendy wysyłane są pomiędzy węzłami za pomocą protokołu TCP.

Przykład komunikacji pomiędzy węzłami:
- uruchamiamy pierwszy węzeł na porcie 9000 z zasobami A:1
java NetworkNode -ident 1 -tcpport 9000 A:1
- uruchamiamy drugi węzeł na porcie 9001 korzystając z pierwszego jako gateway z zasobami B:2
java NetworkNode -ident 2 -tcpport 9001 -gateway localhost:9000 B:2
- w tym momencie węzeł 2 wysyła komunikat do węzła 1
NEW_NODE 9001
- węzeł 1 aktualizuje nodesList o adres ip i port nowego węzła
- aktualnie uruchamiany węzeł 2 wpisuje adres gateway do swojego nodesList
- uruchamiamy trzeci węzeł na porcie 9002 korzystając z drugiego jako gateway z zasobami C:1
java NetworkNode -ident 3 -tcpport 9002 -gateway localhost:9001 C:1
- w tym momencie węzeł 3 wysyła komunikat do węzła 2
NEW_NODE 9002
- w odpowiedzi otrzymuje listę adresów ip i portów, które węzeł 3 ma w nodesList (ip/port węzła 1). Uruchamiany węzeł zapisuje listę w swoim nodesList oraz uzupełnia o adres gateway.
- następnie węzeł 2 wysyła informację do wszystkich węzłów w swojej nodesList (w tym momencie tylko do węzła 1) o zarejestrowaniu nowego węzła 3
UPDATE_NODE_LIST localhost:9002
- węzeł 1 aktualizuje swoją nodesList w oparciu o komunikat UPDATE_NODE_LIST

Przykład komunikacji w przypadku wyłączania sieci
- uruchamiamy 3 węzły
java NetworkNode -ident 1 -tcpport 9000 A:1
java NetworkNode -ident 2 -tcpport 9001 -gateway localhost:9000 B:1
java NetworkNode -ident 3 -tcpport 9002 -gateway localhost:9000 C:1
- za pomocą klienta wywołujemy komendę terminate używając węzła nr 3 jako gateway
java NetworkClient -ident 1 -gateway localhost:9002 terminate
- węzęł nr 3 wysyła do pozostałych węzłów komunikat TERMINATE_NODE co powoduje ich wyłączenie
- węzeł nr 3 sam siebie wyłącza
- wszystkie węzły zostały wyłączone

Przykład rezerwacji zasobów w obrębie jednego węzła
- uruchamiamy węzeł z odpowiednimi zasobami
java NetworkNode -ident 1 -tcpport 9000 A:1 B:2
- próbujemy zarezerwować więcej zasobów niż dostępne
java NetworkClient -ident 1 -gateway localhost:9002 A:2 B:1
- zasoby nie zostają zarezerwowane
- próbujemy zarezerować odpowiednią ilość zasobów
java NetworkClient -ident 1 -gateway localhost:9002 A:1 B:1
- zasoby zostają zarezerowane i do rezerwacji pozostaje A:1


2. URUCHOMIENIE
-----------------------------------------------
Należy skopilować program
javac NetworkNode.java

lub skorzystać ze skryptu "compile.sh"

Następnie w linii poleceń należy wpisać do uruchomiania pierwszego węzła
java NetworkNode -ident <id> -tcpport <nr_portu> <nazwa_zasobu>:<ilość_zasobu>

lub skorzystać ze skryptu "start.sh", który uruchamia 3 węzły na portach 9000, 9001, 9002


3. CO ZOSTAŁO ZAIMPLEMENTOWANE
-----------------------------------------------
- organizacja sieci i komunikacja między węzłami podczas powiększania sieci o nowe węzły
- wyłączanie wszystkich węzłów gdy jeden z nich odbierze komunikat "TERMINATE"
- odbieranie żądań od klienta o rezerwację zasobów w obrębie jednego węzła


4. CO NIE DZIAŁA
-----------------------------------------------
- Rezerwacja zasobów działa wyłącznie w obrębie węzła, do którego klient się łączy. Brakuje przeszukiwania pozostałych węzłów pod kątem dostępnych zasobów.
- Komunikacja za pomocą UDP


