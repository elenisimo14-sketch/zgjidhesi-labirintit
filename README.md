# Zgjidhësi i labirintit

Vizualizim i rrugës dhe i qelizave të vizituara.

Lënda: Algoritme të Avancuara dhe Struktura të Dhënash
Universiteti POLIS, Master i Shkencave në Shkenca Kompjuterike, 2026/2027

**Gjendja: faza e parë e implementimit.** Baza e përbashkët dhe algoritmi i parë janë gati. DFS, Dijkstra dhe A\* janë ndarë midis anëtarëve dhe vijnë në fazat e ardhshme.

---

## Nga ku nisëm

Nuk nisëm nga algoritmet, por nga kontrata midis tyre. Radha e punës ishte kjo:

**1. Përfaqësimi.** Labirinti ruhet si matricë `int[][]` me 0 për qelizë të lirë dhe 1 për mur. Ky vendim përcakton gjithçka tjetër: leximin, vizatimin, dhe mënyrën si gjenden fqinjët.

**2. Kthimi i matricës në graf.** Metoda `Labirint.fqinjet()` kthen qelizat fqinje të lira të një qelize. Kjo është lista e fqinjësisë së grafit. Pa të, nuk ekziston asnjë algoritëm grafesh.

**3. Çfarë kthen një algoritëm.** Kjo ishte pyetja më e rëndësishme. Nëse një algoritëm kthen vetëm rrugën, procesi i kërkimit nuk mund të vizualizohet, dhe vizualizimi i procesit është pikërisht tema jonë. Prandaj `Rezultat` mban rrugën, metrikat **dhe historikun**: çdo qelizë që u zbulua dhe çdo qelizë që u zgjerua, me radhë.

**4. Kontrata.** Klasa abstrakte `Zgjidhes` ka një metodë: merr një labirint, kthen një rezultat. Pasi kjo u vendos, të katër anëtarët mund të punojnë paralelisht, sepse secili e di saktësisht çfarë merr dhe çfarë duhet të kthejë.

**5. BFS.** I shkruar i pari jo sepse është algoritëm, por sepse është prova që kontrata funksionon. Rruga që kthen mund të verifikohet me dorë, prandaj BFS bëhet gurthemeli i testimit për të tre algoritmet e ardhshme.

**6. Vizualizimi.** Vetëm në fund, mbi listën e hapave që BFS kthen.

---

## Vendimi kryesor arkitekturor

**Algoritmi nuk vizaton.** Ai ekzekutohet i tëri dhe kthen një listë hapash; paneli e luan atë listë më pas me `javax.swing.Timer`.

Nëse algoritmi do të vizatonte vetë, do të duhej një pauzë brenda ciklit të tij. Në Swing kjo bllokon Event Dispatch Thread dhe dritarja ngrin: nuk do të dukej asgjë derisa kërkimi të mbaronte, pra e kundërta e qëllimit tonë.

Kjo ndarje solli edhe një përfitim që nuk e kishim parashikuar: algoritmet mund të testohen pa hapur asnjë dritare.

---

## Ekzekutimi

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")

java -cp out labirint.Main                      # ndërfaqja grafike
java -cp out labirint.Main konsole              # varianti në konsolë
java -cp out labirint.verifikim.Verifikues      # 16 kontrolle saktësie
```

Në Windows PowerShell:

```
javac -encoding UTF-8 -d out (ls -r src\main\java -Filter *.java).FullName
java -cp out labirint.Main
```

Me Maven: `mvn clean test` dhe `mvn clean package`.

---

## Struktura e kodit

```
src/main/java/labirint/
  Main.java                     pika e hyrjes
  model/
    Pozicion.java               koordinatat, distanca Manhattan
    Labirint.java               matrica, fqinjët, validimi
    Hap.java                    një hap i vetëm i kërkimit
    LlojHapi.java               NE_KUFI ose VIZITUAR
    Rezultat.java               rruga, metrikat, historiku
  algo/
    Zgjidhes.java               kontrata e përbashkët, ndërtimi i rrugës
    ZgjidhesBFS.java            radhë FIFO, ArrayDeque
  ui/
    Dritarja.java               kontrollet dhe përbërja
    PanelLabirinti.java         animacioni me Timer
    PanelStatistikave.java      tabela e krahasimit
  verifikim/
    Verifikues.java             16 kontrolle pa varësi të jashtme
```

---

## Të dhënat

Labirinti i referimit 14×23, i ndërtuar nga grupi, te `data/labirinti-referimi.txt`.

| Karakteristika | Vlera |
| --- | --- |
| Qeliza gjithsej | 322 |
| Qeliza të lira | 147 |
| Mure | 175 |
| Hyrja | (4, 0) |
| Dalja | (13, 21) |
| Rruga optimale | 35 qeliza |
| Qeliza të arritshme nga hyrja | 135 |

Dymbëdhjetë qeliza të lira ndodhen në xhepa të izoluar. Kjo nuk është defekt: na jep rast testimi për sjelljen e algoritmeve kur ka zona të paarritshme.

---

## Rezultati i parë

| Algoritmi | Gjatësia e rrugës | Qeliza të vizituara |
| --- | --- | --- |
| BFS | 35 | 104 |

Vini re dallimin midis 104 dhe 135. BFS ndalon sapo e gjen daljen, prandaj nuk i prek të gjitha qelizat e arritshme. Ky ndalim i hershëm nuk e cenon optimalitetin: kur dalja del nga radha, asnjë rrugë më e shkurtër nuk mund të ekzistojë.

---

## Testimi

Rezultati i pritshëm nuk vjen nga implementimi që testohet. Numri 135 i qelizave të arritshme llogaritet me një përmbytje BFS të pavarur, të shkruar brenda vetë testit. Gjatësia 35 është verifikuar edhe me dorë mbi matricë.

Mbulimi: struktura e labirintit, optimaliteti i BFS, vlefshmëria e rrugës, regjistrimi i historikut, labirint pa zgjidhje, dhe katër raste validimi të hyrjes së pavlefshme.

---

## Ndarja e punës

Baza, pra paketa `model` dhe kontrata `Zgjidhes`, u vendos së bashku para ndarjes. Pastaj secili anëtar mori një pjesë të kësaj faze dhe një algoritëm për fazat e ardhshme.

| Anëtari | Pjesa në këtë fazë | Skedarët | Algoritmi që merr më pas |
| --- | --- | --- | --- |
| Anëtari 1 | Modeli dhe grafi | `Pozicion`, `Hap`, `LlojHapi`, `Rezultat`, `Labirint` | BFS |
| Anëtari 2 | Kontrata dhe BFS | `Zgjidhes`, `ZgjidhesBFS` | DFS |
| Anëtari 3 | Vizualizimi | `PanelLabirinti`, `PanelStatistikave`, `Dritarja`, `Main` | Dijkstra |
| Anëtari 4 | Verifikimi dhe të dhënat | `Verifikues`, `ZgjidhesTest`, `labirinti-referimi.txt` | A\* |

Struktura që përdor secili algoritëm:

| Algoritmi | Struktura |
| --- | --- |
| BFS | `ArrayDeque` si radhë FIFO |
| DFS | `ArrayDeque` si stek LIFO |
| Dijkstra | `PriorityQueue` sipas kostos reale |
| A\* | `PriorityQueue` sipas `f = g + h` |

Çdo anëtar punon në degën e vet (`anetari-2-bfs`, `anetari-3-vizualizimi`, `anetari-4-verifikimi`), dhe puna bashkohet te `main` përmes Pull Request-eve në rendin 1, 2, 3, 4, sepse pjesët varen nga njëra-tjetra.

---

## Çfarë vjen më pas

DFS, Dijkstra dhe A\* si nënklasa të `Zgjidhes`, pa prekur ndërfaqen. Pastaj gjenerimi automatik i labirinteve, databaza për matjet, hartat publike si dataset real, dhe eksperimentet krahasuese.
