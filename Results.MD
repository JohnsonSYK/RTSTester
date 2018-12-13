# Testing Results for RTS Tester
###RTS Tools Evaluation:
I tested my tool on the following projects over 100 SHAs with Ekstazi, and here are the results:

| | Tests w/o RTS | Tests w/ RTS |%Tests selected | Time w/o RTS(s) |  Time w/ RTS(s) | %Time used |
| :---:        |     :---:      |         :---: |  :---:|  :---:|  :---:| :---:|
| perwendel/spark   |   302  |  73.4   | 24.3% | 78.6| 22| 27.98%|
| jhy/jsoup     |    668    |    478.86   | 71.56%|11.65 |9.5 | 81.52|
| JodaOrg/joda-time     |   4223     |  525.54     | 12.44%|19.5 | 9.88| 51.5%|
| AsyncHttpClient/async-http-client     |    870     |   344.73    |39.5% | 210.94|201.06 |95.7% |
| apache/pdfbox     |     1633    |   892    |54.6% |104 |65.5 |63.0% |
| vipshop/vjtools     |    221  |   188    | 85%|17.52 |14.6 | 83.4%|
| kevinsawicki/http-request    |   160     |     53.8  |33.63% |6.98 | 4.26|61.03% |
| iTrust    |     847.5   |   143    | 16.9%| 34.9|13.6 | 39%|
| larsga/Duke    |   662     |  115    |17.37% |72 |25 | 34.7%|





###Test Generation:

I tested my tool on the following projects with Evosuite, and here are the results:

| | #Tests (before) | #Tests (after)  | Test generated | Coverage (before) | Coverage(after) | Improvement |
| :---:        |     :---:      |         :---: |  :---:|  :---:|  :---:| :---:|
| perwendel/spark   |   304  |  1637   | 1333 | 68%| 72%| 4%|
| jhy/jsoup     |    669    |   3101   | 2432|84% |84% |0%|
| JodaOrg/joda-time     |   4226     |  19661  | 15435|84% | 89%| 5%|
| tutorial_maven     |   1     |   27    |26 | 5%|98% |93% |
| kevinsawicki/http-request     |     161    | 354    |193 |51% |73% |22% |
| apache/pdfbox    |  1633  |  2092    |459|95% |95% | 0%|
| vipshop/vjtools   |   221     |   758  |537 |90% | 92%|2% |
| openmrs-core   |     0   |   16    | 16| 0%|79% | 79%|