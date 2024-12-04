<?php
require_once 'phplot.php';
require_once 'database_cfg.php';
if(isset($_GET['id_dane']) && isset($_GET['WL_START']) && isset($_GET['WL_STOP'])) 
{
    $id_dane = $_GET['id_dane'];
    $result = mysqli_query($connection, "SELECT * FROM dane WHERE id_dane ='$id_dane'");
    $wiersz = mysqli_fetch_row($result);
    $nazwa = $wiersz[1];
    $id_pomiar_start = $wiersz[2];
    $id_pomiar_end = $wiersz[3];
    //$x_start = $wiersz[6];
    //$x_end = $wiersz[7];
    $WL_START = $_GET['WL_START'];
    $WL_STOP = $_GET['WL_STOP'];
    $result_1 = mysqli_query($connection, "SELECT * FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND x='$WL_START'");
    $wiersz_1 = mysqli_fetch_row($result_1);
    $pomiar_start = $wiersz_1[0];
    $result_2 = mysqli_query($connection, "SELECT * FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND x='$WL_STOP'");
    $wiersz_2 = mysqli_fetch_row($result_2);
    $pomiar_stop = $wiersz_2[0];

    $res = mysqli_query($connection, "SELECT MIN(y) FROM pomiary WHERE id_pomiar>='$pomiar_start' AND id_pomiar<='$pomiar_stop' AND y>=0.00003");
    $test = mysqli_fetch_row($res);
    $y_min = $test[0];

    //set new x_start and x_stop
    $res_min = mysqli_query($connection, "SELECT x FROM pomiary WHERE id_pomiar>='$pomiar_start' AND id_pomiar<='$pomiar_stop' AND y>=0.00003 ORDER BY id_pomiar ASC LIMIT 1");
    $wiersz_min = mysqli_fetch_row($res_min);
    $start = $wiersz_min[0];
    $res_max = mysqli_query($connection, "SELECT x FROM pomiary WHERE id_pomiar>='$pomiar_start' AND id_pomiar<='$pomiar_stop' AND y>=0.00003 ORDER BY id_pomiar DESC LIMIT 1");
    $wiersz_max = mysqli_fetch_row($res_max);
    $stop = $wiersz_max[0];

    $rezultat = mysqli_query($connection, "SELECT x,y FROM pomiary WHERE id_pomiar>='$pomiar_start' AND id_pomiar<='$pomiar_stop' AND y>=0.00003 ORDER BY id_pomiar");
    $plot = new PHPlot();
    if (!$rezultat) exit();
    $data = array();
    $n_rows = mysqli_num_rows($rezultat);
    for ($i = 0; $i < $n_rows; $i++)
    {
        $wiersz2 = mysqli_fetch_row($rezultat);
        $x = $wiersz2[0];
        $y = $wiersz2[1];
        $data[] = array('',$x,$y);
    }
    $plot->SetDataType('data-data');
    $plot->SetDataValues($data);
    $plot->SetYTickPos('none');
    $plot->SetXTickPos('none');
    $plot->SetFontTTF('x_title', 'DejaVuSansCondensed.ttf', 10);
    $plot->SetFontTTF('y_title', 'DejaVuSansCondensed.ttf', 10);
    $plot->SetFontTTF('title', 'DejaVuSansCondensed.ttf', 16);
    $plot->SetFontTTF('y_label', 'DejaVuSansCondensed-Bold.ttf', 6);
    $plot->SetFontTTF('x_label', 'DejaVuSansCondensed-Bold.ttf', 6);

    $plot->SetXTitle('Długość fali [nm]');
    $plot->SetYTitle('Moc sygnału optycznego [W]');
    $plot->SetTitleColor(array(255, 172, 28));
    $plot->SetXTitleColor(array(255, 255, 0));
    $plot->SetYTitleColor(array(255, 255, 0));
    $plot->SetBackgroundColor(array(88, 92, 95));
    $plot->SetPlotAreaWorld($start, $y_min, $stop, NULL); //$WL_START , $WL_STOP
    $plot->SetXTickLabelPos('xaxis');
    $plot->SetPrecisionX(0);
    $plot->SetPrecisionY(4);
    $plot->SetTextColor(array(255, 255, 255));
    $plot -> SetPlotType('lines');
    $plot -> SetTitle($nazwa);
    $plot -> DrawGraph();
}
else if(isset($_GET['id_dane']))
{
//$id_dane = $_POST['id_dane'];
$id_dane = $_GET['id_dane'];
$result = mysqli_query($connection, "SELECT * FROM dane WHERE id_dane ='$id_dane'");
$wiersz = mysqli_fetch_row($result);
$nazwa = $wiersz[1];
$id_pomiar_start = $wiersz[2];
$id_pomiar_end = $wiersz[3];
$x_start = $wiersz[6];
$x_end = $wiersz[7];

$res = mysqli_query($connection, "SELECT MIN(y) FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND y>=0.00003");
$test = mysqli_fetch_row($res);
$y_min = $test[0];

$res_min = mysqli_query($connection, "SELECT x FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND y>=0.00003 ORDER BY id_pomiar ASC LIMIT 1");
$wiersz_min = mysqli_fetch_row($res_min);
$start = $wiersz_min[0];
$res_max = mysqli_query($connection, "SELECT x FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND y>=0.00003 ORDER BY id_pomiar DESC LIMIT 1");
$wiersz_max = mysqli_fetch_row($res_max);
$stop = $wiersz_max[0];

$rezultat = mysqli_query($connection, "SELECT x,y FROM pomiary WHERE id_pomiar>='$id_pomiar_start' AND id_pomiar<='$id_pomiar_end' AND y>=0.00003 ORDER BY id_pomiar");
$plot = new PHPlot();
if (!$rezultat) exit();
$data = array();
$n_rows = mysqli_num_rows($rezultat);
for ($i = 0; $i < $n_rows; $i++)
{
    $wiersz2 = mysqli_fetch_row($rezultat);
    $x = $wiersz2[0];
    $y = $wiersz2[1];
    $data[] = array('',$x,$y);
}
$plot->SetDataType('data-data');
$plot->SetDataValues($data);
$plot->SetYTickPos('none');
$plot->SetXTickPos('none');
$plot->SetFontTTF('x_title', 'DejaVuSansCondensed.ttf', 10);
$plot->SetFontTTF('y_title', 'DejaVuSansCondensed.ttf', 10);
$plot->SetFontTTF('title', 'DejaVuSansCondensed.ttf', 16);

$plot->SetFontTTF('y_label', 'DejaVuSansCondensed-Bold.ttf', 6);
$plot->SetFontTTF('x_label', 'DejaVuSansCondensed-Bold.ttf', 6);
$plot->SetXTitle('Długość fali [nm]');
$plot->SetYTitle('Moc sygnału optycznego [W]');
$plot->SetTitleColor(array(255, 172, 28));
$plot->SetXTitleColor(array(255, 255, 0));
$plot->SetYTitleColor(array(255, 255, 0));
$plot->SetBackgroundColor(array(88, 92, 95));
//$plot->SetXScaleType('linear');
//$plot->SetPlotAreaWorld($x_start, $y_min - 0.00001, $x_end, NULL);
$plot->SetPlotAreaWorld($start, $y_min, $stop, NULL);
$plot->SetXTickLabelPos('xaxis');
$plot->SetPrecisionX(0);
$plot->SetPrecisionY(4);
$plot->SetTextColor(array(255, 255, 255));
$plot -> SetPlotType('lines');
$plot -> SetTitle($nazwa);
$plot -> DrawGraph();
}
else
{
    header("Location: data.php");
}
?>