<?php
// Find all webcam images
$imageList = @scandir('webcam', 1);
if ($imageList === FALSE) {
	echo '<p>Directory scan error.</p>';
}

// Only take the newest and extract metadata
$newestImage['name'] = array_shift($imageList);
$nameParts = explode('_', $newestImage['name']);
$nameParts[0] = substr($nameParts[0], 14);
$newestImage['date'] = substr($nameParts[0], 6, 2) . '.'
	. substr($nameParts[0], 4, 2) . '.'
	. substr($nameParts[0], 0, 4) . ' '
	. substr($nameParts[1], 0, 2) . ':'
	. substr($nameParts[1], 2, 2) . ':'
	. substr($nameParts[1], 4, 2);

// Delete old images
foreach($imageList as $oldImage) {
	@unlink('webcam/' . $oldImage);
}

// Parse current conditions
$lines = file('result.txt');
$conditions['date'] = trim($lines[0]);
$conditions['values'] = array();
for ($i = 2; $i <= count($lines)-1; $i++) {
	// Extract information in the format <desc>[<unit>]:<value>;
	if (preg_match('/(\w+)\[([^\]]+)\]:([^;]+)/', $lines[$i], $lineParts)) {
		$lineParts[3] = round(floatval($lineParts[3]), 1);
		$lineParts[4] = $lineParts[5] = '';
		// also show km/h for windspeed
		if ($lineParts[1] == 'Windspeed') {
			$lineParts[1] = 'Windgeschwindigkeit';
			$lineParts[4] = round($lineParts[3] * 3.6, 1);
			$lineParts[5] = 'km/h';
		}
		if ($lineParts[1] == 'Windrichtung') {
			$lineParts[2] = '°';
		}
		$conditions['values'][] = $lineParts;
	}
}

?>
<!doctype html>
<html lang="de">
<head>
  <meta charset="utf-8">
  <title>Wetterdaten und Webcam des MFC Ikarus Ohlsdorf</title>
  <meta name="description" content="Webcam und Wetterdaten">
  <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css" />
  <script src="http://code.jquery.com/jquery-1.8.3.js"></script>
  <script src="http://code.jquery.com/ui/1.9.2/jquery-ui.js"></script>
  <style type="text/css">
  body {
  	  font-size: 80%;
  }
  table { text-align: left; }
  #weather img { display: block; margin: 0 auto; }
  #winddirection {
  	  background: url('ohlsdorf.jpg') no-repeat center 23px;
  }
  #tabs-outer {
  	  width: 750px;
  }
  #footer img { vertical-align: middle; border: 0; }
  </style>
  <script type="text/javascript">
    $(function() {
  	  $( "#tabs" ).tabs({
		select: function( event, ui ) {
			window.location.replace(ui.tab.hash);
		}
	  });
  	});
	setTimeout("location.reload(true)" , 60000);
  </script>
</head>
<body>
<div id="tabs-outer">
<div id="tabs">
 <ul>
  <li><a href="#tabs-1">Webcam</a></li>
  <li><a href="#tabs-2">Aktuelles Wetter</a></li>
  <li><a href="#tabs-3">Windrichtung</a></li>
  <li><a href="#tabs-4">Verlauf (24 Stunden)</a></li>
 </ul>

<div id="tabs-1">
<img src="webcam/<?php echo htmlspecialchars($newestImage['name']); ?>" width="704" alt="Aktuelles Webcam-Bild" />
</div>

<div id="tabs-2">
<p><strong>Aktuelle Werte (<?php echo htmlspecialchars($conditions['date']); ?>)</strong></p>
<table>
<?php foreach ($conditions['values'] as $value) {
	echo '<tr><td>';
	echo htmlspecialchars($value[1]) . ':';
	echo '</td><td>';
	echo htmlspecialchars($value[3] . ' ' . $value[2]);
	if (!empty($value[5])) {
		echo ' (' . htmlspecialchars($value[4] . ' ' . $value[5]) . ')'; 
	}
	echo '</td></tr>';
?>
<?php } ?>
</table>
</div>

<div id="tabs-3">
<img src="Flugplatz_Windrichtung_c0.png?t=<?php echo date("YMdHs"); ?>" width="600" id="winddirection" alt="Aktuelle Windrichtung" />
</div>

<div id="tabs-4">
<img src="Flugplatz_AussentemperaturFlugplatz_Windspeed1.png?t=<?php echo date("YMdHs"); ?>" width="600" alt="Außentemperatur und Windgeschwindigkeit der letzten 24h" /><br /><br />
<img src="Flugplatz_Innentemperatur5.png?t=<?php echo date("YMdHs"); ?>" width="600" alt="Innentemperatur der letzten 24h" />
</div>

</div>
</div>
<p>(<em>Diese Seite wird automatisch jede Minute aktualisiert.</em>)</p>
<p id="footer"><strong>&copy</strong> <a href="http://www.akatec.at/" target="_blank"><img src="akatec.jpg" width="100" alt="Akatec IT-Systemintegration GmbH" /></a></p>
</body>
</html>
