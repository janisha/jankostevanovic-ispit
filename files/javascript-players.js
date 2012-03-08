$(document).ready(function(){
						
		$('.pozovi_za_multiplayer').click(function(){
			var link = $(this).attr('href');
			var user = $(this).html();			
			var user_id = link;			
			$.post('ajax-zovi-igraca',{id: user_id}, function(data){
				alert(data);
			})
			return false;			
		});
		
		function update_log_time(){
			$.post('ajax-log-refresh-players', function(data){
				$('#lista-igraca').html(data);	
				$('.pozovi_za_multiplayer').live('click', function(){
					var link = $(this).attr('href');
					var user = $(this).html();			
					var user_id = link;			
					$.post('ajax-zovi-igraca',{id: user_id}, function(data){
						alert(data);
					})
					return false;
				});
			});
		}
			
		window.setInterval(update_log_time, 3000);
		
		function call_check(){
			// Treba da se vrate odgovor i ID multiplayer igre
			$.post('ajax-call-check', function(data){
				if(data != "nema")
					{
						
						var lista_odgovor = data.split(",");
						
						var odgovor = lista_odgovor[0].split(":");
							odgovor = odgovor[0];
						var id_call = lista_odgovor[1].split(":");
							id_call = id_call[1];
						var id_game = lista_odgovor[2].split(":");
							id_game = id_game[1];
												
						switch(odgovor)
						{
							case "pozvan_od": {
												var prihvatam = confirm('Da li prihvatate izazov?');
												if(prihvatam == true) //- poziv je prihvacen
													$.post('ajax-prihvatam-igru',{id_call: id_call},function(data){	
														window.location = "/multiplayer/"+id_game;
													});									
												else //-- poziv je odbijen
													$.post('ajax-odbijam-igru',{id_call:  id_call},function(){
													}); 
												break;
											  }
							case "prihvacen_od": {
													alert("Igra vam je prihvacena..");
													//-- brise staru igru i ulazi u igru..
													$.post('ajax-odbijam-igru',{id_call:  id_call},function(){}); 													
													window.location = "/multiplayer/"+id_game;
													break;
												}							
						}
				/*		
						if("pozvan_od" = odgovor);
							{
								var prihvatam = confirm('Da li prihvatate izazov?');
								if(prihvatam == true)
									$.post('ajax-prihvatam-igru',{id_call: id_call},function(data){
										//- poziv je prihvacen
										alert(data);
										window.location = "/multiplayer";
									});									
								else
									$.post('ajax-odbijam-igru',{id_call:  id_call},function(){
										//- poziv je odbijen
									});
							}
					    if("prihvacen_od" == odgovor)
							{
								alert("Vas izazov je prihvacen.");
								//window.location = "/multiplayer";
							}
				*/		
							
					}
				//alert(data);
				//$('#ajax_odgovori').html(data);
			});
		}
		
		window.setInterval(call_check, 4000);
});
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	