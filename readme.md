# Implementasi Model Probabilistik Information Retrieval

## Cara Menjalankan Program
1. Buka terminal atau command prompt pada direktori utama projek.
2. Compile seluruh file Java.
3. Run kelas utama, yaitu `Main.java`.
4. Program akan membaca dataset, membangun Inverted Index, melakukan evaluasi pada kueri uji, dan menampilkan metrik hasil (Precision, Recall, Precision@K, dan 11-Point Average Precision) pada terminal.

## Penjelasan Formula Model Probabilistik
### 1. Binary Independence Model (BIM)
BIM menggunakan asumsi independensi term dan representasi binary variable acak. Karena implementasi awal ini disesuaikan tanpa menggunakan informasi relevance judgements (Skenario 1), estimasi bobot dari tiap term dihitung berdasarkan formula:
$$w_t = \log \left( 0.5 \frac{N}{N_t} \right)$$
Keterangan:
- $N$ = Total dokumen di dalam koleksi dokumen.
- $N_t$ = Banyaknya dokumen yang mengandung term t (document frequency).

### 2. Two-Poisson Model
Model ini mengasumsikan bahwa frekuensi term mengikuti dua distribusi Poisson yang berbeda untuk dokumen yang relevan dan dokumen yang tidak relevan. Perhitungan skor relevansi murni menggunakan frekuensi mentah term tanpa melakukan normalisasi pada panjang dokumen.
$$rel(D,Q) = \sum_{t \in Q} \frac{f_{t,D}(k+1)w_t}{f_{t,D} + k}$$
Keterangan:
- $f_{t,D}$ = Frekuensi mentah (term frequency) dari term t pada dokumen D.
- $k$ = Nilai konstan parameter (bawaan rentang $1 \le k < 2$, diatur sebesar 1.2).
- $w_t$ = Bobot term yang didapat dari perhitungan BIM.

### 3. BM11
BM11 merupakan varian dari keluarga model Best Matching yang memperbaiki bobot Two Poisson model untuk mengatasi perbedaan panjang dokumen. Model ini memasukkan faktor normalisasi panjang dokumen ke dalam perhitungannya.
$$rel(D,Q) = \sum_{t \in Q} \frac{f_{t,D}(k+1)w_t}{f_{t,D} + \frac{k(l_d)}{l_{avg}}}$$
Keterangan:
- $l_d$ = Panjang dari dokumen spesifik D.
- $l_{avg}$ = Rata-rata panjang keseluruhan dokumen dalam koleksi.

### 4. BM25
BM25 merupakan modifikasi lanjutan dari keluarga model BM untuk mengatasi masalah pada model sebelumnya dengan memasukkan parameter b. Formula ini berfungsi untuk mengeksekusi document length normalization terhadap rata-rata panjang dokumen (avgdl).
$$rel(D,Q) = \sum_{t \in Q} \frac{f_{t,D}(k+1)w_t}{f_{t,D} + \frac{k(l_d)}{l_{avg}}b + k(1-b)}$$
Keterangan:
- $l_d$ = Panjang dari dokumen D (document length).
- $l_{avg}$ = Rata-rata panjang dokumen (avgdl) di dalam seluruh koleksi dokumen.
- $b$ = Parameter kontrol untuk mengatur seberapa besar efek normalisasi panjang dokumen (diatur sebesar 0.75).

## Kesimpulan Evaluasi Model
Berdasarkan metrik evaluasi yang diuji pada dataset, terdapat perbedaan karakteristik kinerja dari keempat model probabilistik:
1. Binary Independence Model (BIM): Model ini memiliki performa paling dasar karena hanya melihat kemunculan term secara biner tanpa memperhitungkan term frequency. 
2. Two-Poisson Model: Model ini lebih baik dari BIM karena sudah memperhitungkan term frequency, namun memiliki kekurangan karena mengasumsikan bahwa semua dokumen memiliki panjang yang sama.
3. BM11: Penambahan komponen normalisasi panjang dokumen membuat model ini bisa menyesuaikan skor berdasarkan panjang dokumen. Namun, dokumen yang terlalu panjang bisa saja mendapatkan penalti yang terlalu besar sehingga skor relevansinya turun secara drastis.
4. BM25: Model ini memberikan performa evaluasi (precision dan recall) yang paling optimal karena parameter b mengontrol seberapa ketat efek dari normalisasi panjang dokumen. Hal ini menjadikan sistem pemeringkatan lebih adil dan stabil untuk dokumen pendek maupun panjang.

## Pembagian Peran Anggota Kelompok
- **Prospero Phelix**: Data & Indexing Engineer (Bertanggung jawab penuh dalam tahap text preprocessing yang meliputi tokenization, stopword removal, dan stemming, serta membangun struktur Inverted Index untuk menyimpan term frequency dan document frequency. Selain itu, bertugas mengimplementasikan algoritma pencarian dasar menggunakan Binary Independence Model atau BIM dan Two-Poisson Model).
- **Agape Dimas**: Probabilistic Model & Evaluation Engineer (Bertanggung jawab dalam implementasi algoritma model lanjutan seperti varian BM11, dan formula BM25, termasuk penyesuaian parameter k dan b beserta perhitungan normalisasi panjang dokumen. Selain itu, bertugas mengintegrasikan seluruh matriks evaluasi kinerja, seperti precision, recall, precision@K, dan 11-point average precision).