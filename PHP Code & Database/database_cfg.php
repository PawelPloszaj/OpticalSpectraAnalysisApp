<?php

$connection = mysqli_connect("sql.serwer2264869.home.pl", "36726788_opticalspectra", "ncmzAHd2epyG", "36726788_opticalspectra"); // połączenie z BD – wpisać swoje dane
$connection->set_charset("utf8");
if ($connection->connect_error) 
{
    die("Connection failed: " . $connection->connect_error);
}
?>