:note:
# Marlow Navigation IO
---
### How to
#### Default configuration
```
default-margin-top = 36
default-margin-bottom = 36
default-margin-left = 36
default-margin-right = 36
table-header-bg-color = 0.65
table-row-bg-color = 0.90
```
#### Generate PDF report
``` 
case class Person(id: UUID, name: String, surname: String, dob: String, balance: BigDecimal)
val dataset: Seq[Person] = ???
val destination: String = "./src/test/resources/mn-report.pdf"
val pdfReport = PdfReport(dataset, destination, "header text", "footer text")
PdfUtils.generateReport(pdfReport)
```
The above code will generate a report with default settings for the provided dataset to the corresponding
destination

---

#### Generate CSV report
:construction_worker: 

---

#### Generate XLS (?) report
:construction_worker:

---
