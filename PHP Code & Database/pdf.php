<?php
require_once 'database_cfg.php';
require('fpdf/tfpdf.php');

if(isset($_GET['id_wynik']))
{
$id_wynik = $_GET['id_wynik'];
$pdf = new tFPDF();
$title = 'Wyniki';
$pdf->SetTitle($title);
$pdf->SetAuthor('Paweł Płoszaj');
$pdf->AddPage('L','A3');
$pdf->AddFont('DejaVuBold','','DejaVuSansCondensed-Bold.ttf',true);
$pdf->SetFont('DejaVuBold','',10);



$w = array(40,25,40,40,40,40,40,40,40,40,40);
$header = array("nazwa_pliku","id_dane","V","x","global_max_X","global_max_Y","srednia_odl","odl_central_lewo","odl_central_prawo","absorpcja","datetime");

$pdf->Cell($w[0],6,$header[0],1,0,'C');
//$pdf->Cell($w[1],6,$header[1],1,0,'C');
$pdf->Cell($w[2],6,$header[2],1,0,'C');
$pdf->Cell($w[3],6,$header[3],1,0,'C');
$pdf->Cell($w[4],6,$header[4],1,0,'C');
$pdf->Cell($w[5],6,$header[5],1,0,'C');
$pdf->Cell($w[6],6,$header[6],1,0,'C');
$pdf->Cell($w[7],6,$header[7],1,0,'C');
$pdf->Cell($w[8],6,$header[8],1,0,'C');
$pdf->Cell($w[9],6,$header[9],1,0,'C');
$pdf->Cell($w[10],6,$header[10],1,0,'C');

$pdf->AddFont('DejaVu','','DejaVuSansCondensed.ttf',true);
$pdf->SetFont('DejaVu','',10);

for($i = 0; $i < count($id_wynik); $i=$i+1)
{
    

$id = $id_wynik[$i];
$result = mysqli_query($connection, "SELECT * FROM wyniki WHERE id_wynik='$id'") or die ("Błąd zapytania do bazy !");

while ($wiersz = mysqli_fetch_array ($result)) 
{
    $id_dane = $wiersz[1];
    $result_second= mysqli_query($connection, "SELECT * FROM dane WHERE id_dane='$id_dane'") or die ("Błąd zapytania do bazy !");
    while($wiersz_second = mysqli_fetch_array($result_second)) 
    {
        $nazwa = $wiersz_second[1];
    }
    $V = $wiersz[2];
    $x = $wiersz[3];
    $global_max_X = $wiersz[4];
    $global_max_Y = $wiersz[5];
    $srednia_odl = $wiersz[6];
    $odl_central_lewo = $wiersz[7];
    $odl_central_prawo = $wiersz[8];
    $absorpcja = $wiersz[9];
    $datetime = $wiersz[10];

    $pdf->Ln();
    $pdf->Cell($w[0],6,$nazwa,1,0,'C');
    //$pdf->Cell($w[1],6,$id_dane,1,0,'C');
    $pdf->Cell($w[2],6,$V,1,0,'C');
    $pdf->Cell($w[3],6,$x,1,0,'C');
    $pdf->Cell($w[4],6,$global_max_X,1,0,'C');
    $pdf->Cell($w[5],6,$global_max_Y,1,0,'C');
    $pdf->Cell($w[6],6,$srednia_odl,1,0,'C');
    $pdf->Cell($w[7],6,$odl_central_lewo,1,0,'C');
    $pdf->Cell($w[8],6,$odl_central_prawo,1,0,'C');
    $pdf->Cell($w[9],6,$absorpcja,1,0,'C');
    $pdf->Cell($w[10],6,$datetime,1,0,'C');
}
}
$pdf->Output();
mysqli_close($connection);
}
exit();
?>
