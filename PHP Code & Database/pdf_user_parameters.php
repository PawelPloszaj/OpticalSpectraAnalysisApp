<?php
require_once 'database_cfg.php';
require('fpdf/tfpdf.php');

if(isset($_GET['id_wynik_p']))
{
$id_wynik_p = $_GET['id_wynik_p'];
$pdf = new tFPDF();
$title = 'Wyniki';
$pdf->SetTitle($title);
$pdf->SetAuthor('Paweł Płoszaj');
$pdf->AddPage('L','A3');
$pdf->AddFont('DejaVuBold','','DejaVuSansCondensed-Bold.ttf',true);
$pdf->SetFont('DejaVuBold','',10);

//$w = array(20,15,30,20,20,10,20,30,30,30,25,30,30,20,40,35);
$w = array(40,15,40,30,30,25,30,40,40,40,40,35,35,40,40,35);
$header = array("nazwa_pliku","id_dane","ILOSC_MAXIMOW","WL_START","WL_STOP","n","V","x","global_max_X","global_max_Y","srednia_odl","odl_central_lewo","odl_central_prawo","absorpcja","POMIAR_NA_PODSTAWIE","datetime");
$pdf->Cell($w[0],6,$header[0],1,0,'C');
//$pdf->Cell($w[1],6,$header[1],1,0,'C');
$pdf->Cell($w[2],6,$header[2],1,0,'C');
//$pdf->Cell($w[3],6,$header[3],1,0,'C');
//$pdf->Cell($w[4],6,$header[4],1,0,'C');
$pdf->Cell($w[5],6,$header[5],1,0,'C');
$pdf->Cell($w[6],6,$header[6],1,0,'C');
$pdf->Cell($w[7],6,$header[7],1,0,'C');
$pdf->Cell($w[8],6,$header[8],1,0,'C');
$pdf->Cell($w[9],6,$header[9],1,0,'C');
$pdf->Cell($w[10],6,$header[10],1,0,'C');
$pdf->Cell($w[11],6,$header[11],1,0,'C');
$pdf->Cell($w[12],6,$header[12],1,0,'C');
$pdf->Cell($w[13],6,$header[13],1,0,'C');
//$pdf->Cell($w[14],6,$header[14],1,0,'C');
//$pdf->Cell($w[15],6,$header[15],1,0,'C');

$pdf->AddFont('DejaVu','','DejaVuSansCondensed.ttf',true);
$pdf->SetFont('DejaVu','',10);

for($i = 0; $i < count($id_wynik_p); $i=$i+1)
{
    
$id = $id_wynik_p[$i];
$result = mysqli_query($connection, "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p='$id'") or die ("Błąd zapytania do bazy !");

while ($wiersz = mysqli_fetch_array ($result)) 
{
    $id_dane = $wiersz[1];
    $result_second= mysqli_query($connection, "SELECT * FROM dane WHERE id_dane='$id_dane'") or die ("Błąd zapytania do bazy !");
    while($wiersz_second = mysqli_fetch_array($result_second)) 
    {
        $nazwa = $wiersz_second[1];
    }

    $ILOSC_MAXIMOW = $wiersz[2];
    $WL_START = $wiersz[3];
    $WL_STOP = $wiersz[4];
    $n = $wiersz[5];
    $V = $wiersz[6];
    $x = $wiersz[7];
    $global_max_X = $wiersz[8];
    $global_max_Y = $wiersz[9];
    $srednia_odl = $wiersz[10];
    $odl_central_lewo = $wiersz[11];
    $odl_central_prawo = $wiersz[12];
    $absorpcja = $wiersz[13];
    $POMIAR_NA_PODSTAWIE = $wiersz[14];
    $datetime = $wiersz[15];

    $pdf->Ln();
    $pdf->Cell($w[0],6,$nazwa,1,0,'C');
    //$pdf->Cell($w[1],6,$id_dane,1,0,'C');
    $pdf->Cell($w[2],6,$ILOSC_MAXIMOW,1,0,'C');
    //$pdf->Cell($w[3],6,$WL_START,1,0,'C');
    //$pdf->Cell($w[4],6,$WL_STOP,1,0,'C');
    $pdf->Cell($w[5],6,$n,1,0,'C');
    $pdf->Cell($w[6],6,$V,1,0,'C');
    $pdf->Cell($w[7],6,$x,1,0,'C');
    $pdf->Cell($w[8],6,$global_max_X,1,0,'C');
    $pdf->Cell($w[9],6,$global_max_Y,1,0,'C');
    $pdf->Cell($w[10],6,$srednia_odl,1,0,'C');
    $pdf->Cell($w[11],6,$odl_central_lewo,1,0,'C');
    $pdf->Cell($w[12],6,$odl_central_prawo,1,0,'C');
    $pdf->Cell($w[13],6,$absorpcja,1,0,'C');
    //$pdf->Cell($w[14],6,$POMIAR_NA_PODSTAWIE,1,0,'C');
    //$pdf->Cell($w[15],6,$datetime,1,0,'C');
}
}
$pdf->Output();
mysqli_close($connection);
}
exit();
?>
