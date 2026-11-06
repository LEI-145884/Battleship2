# Excerpts for the LLM prompt: God Class (Fowler's Large Class)

Source: Lanza & Marinescu, *Object-Oriented Metrics in Practice*, Springer 2006
(printed page numbers; PDF page = printed page + 11).
The text below is condensed/paraphrased from the book, except for the tables.

## 1. Which Fowler smell it formalises (p. 80, Sect. 5.3)
The God Class design flaw refers to classes that centralize the intelligence of the system.
A God Class performs too much work on its own, delegating only minor details to trivial
classes and using data from other classes. "This design problem is comparable to Fowler's
Large Class bad smell." It applies to **classes**.
The book stresses it is only a real problem if it hampers evolution: a class with these
characteristics in a stable, untouched part of the system is not a problem.

## 2. Detection strategy (p. 53-57 Sect. 4.1; p. 80-81 Sect. 5.3, Fig. 4.5 / 5.2)

    GodClass = (ATFD > FEW)  AND  (WMC >= VERY HIGH)  AND  (TCC < ONE THIRD)

1. **Class uses directly more than a few attributes of other classes**: `ATFD > FEW`.
   The higher ATFD, the higher the probability the class is (or is about to become) a God Class.
2. **Functional complexity of the class is very high**: `WMC >= VERY HIGH`
   (statistical threshold, see Table 2.2 below).
3. **Class cohesion is low**: `TCC < ONE THIRD` (less than one third of method pairs use a
   common attribute). `ONE QUARTER` can be used for more extreme cases (p. 56-57).

All three symptoms must co-exist (AND). The result is a list of *suspects* that must be
inspected manually (p. 57).

## 3. Metric definitions (Appendix A, p. 173-174)
- **WMC** (Weighted Method Count): sum of the cyclomatic complexity (CYCLO) of all methods of a class.
- **TCC** (Tight Class Cohesion): relative number of method pairs of a class that access in
  common at least one attribute of the measured class (0..1; lower = less cohesive).
- **ATFD** (Access To Foreign Data): number of attributes from unrelated classes that are
  accessed directly or by invoking accessor methods.
- **NOM** (Number of Methods): number of methods of a class.

## 4. Thresholds (Chapter 2, p. 15-18)

Table 2.1, Java columns (statistics of 45 Java systems):

| Metric            | Low  | Average | High | Very high |
|-------------------|------|---------|------|-----------|
| CYCLO / Line of code | 0.16 | 0.20 | 0.24 | 0.36 |
| LOC / Method      | 7    | 10      | 13   | 19.5      |
| NOM / Class       | 4    | 7       | 10   | 15        |

Table 2.2, Java columns (derived: WMC = CYCLO/LOC * LOC/Method * NOM/Class):

| Metric     | Low | Average | High | Very high |
|------------|-----|---------|------|-----------|
| WMC        | 5   | 14      | 31   | 47        |
| AMW        | 1.1 | 2.0     | 3.1  | 4.7       |
| LOC/Class  | 28  | 70      | 130  | 195       |
| NOM/Class  | 4   | 7       | 10   | 15        |

Table 2.3, fraction thresholds (p. 17): 0.25 ONE-QUARTER, 0.33 ONE-THIRD, 0.5 HALF,
0.66 TWO-THIRDS, 0.75 THREE-QUARTERS.

Table 2.4, absolute thresholds (p. 18): 0 NONE, 1 ONE/SHALLOW, 2-5 TWO, THREE/FEW/SEVERAL,
7-8 short memory capacity. So `FEW` is in the range 2-5.

## 5. Related strategy: Brain Class (Sect. 5.7, p. 97+)
Complements God Class by catching excessively complex classes that are not detected as God
Classes (e.g. because they do not access foreign data). Uses WMC, TCC and the number of
Brain Methods, with thresholds expressed relative to WMC/NOM/LOC.

## 6. Refactoring hints (p. 83)
Identify clusters of methods and attributes tied together and extract them into separate
classes ("Split Up God Class"; Feathers' Sprout Method / Sprout Class / Wrap Method).

## 7. Concrete threshold values for the Qodana translation
- Large-class size: NOM/Class >= 10 (high) or 15 (very high)
- Complexity: WMC >= 47 (very high; 31 = high)
- Cohesion (TCC < 1/3) and foreign data (ATFD > FEW): no direct Qodana equivalent
