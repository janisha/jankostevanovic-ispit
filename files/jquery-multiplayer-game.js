$(document).ready(function(){
	
	var KRAJ = $('#igra_se').html();
	
	function set_game()
	{
	
			var rec1 = $('#nepoznata1').html();	
			var rec2 = $('#nepoznata2').html();	
			var slova1 = rec1.split("");
			var slova2 = rec2.split("");
			
			nepoznato1 = $('#nepoznato_multi_1');
			nepoznato2 = $('#nepoznato_multi_2');
			
			postavi_slova(slova1, nepoznato1);
			postavi_slova(slova2, nepoznato2);
			
			postavi_lutkicu($('#broj_promasaja_1').html(), 1);
			postavi_lutkicu($('#broj_promasaja_2').html(), 2);
			
			blokiraj_slova($("#birana_slova_1").html(), 1);		
			blokiraj_slova($("#birana_slova_2").html(), 2);
		
	}
	
	function postavi_slova(slova, objekat)
	{
		var slova_prikaz = "";
		
		$.each(slova, function(index, value) { 			
			var slovo = value;
			if(slovo == "-")
				{
					slova_prikaz += "<div class=\"nepoznato-slovo\"></div>";
				}
			else if(slovo == " ")
				{
					slova_prikaz += "<div class=\"nepoznato-slovo-blanko\"> </div>";
				}
			else
				{
					slova_prikaz += "<div class=\"nepoznato-slovo\">"+value+"</div>";
				}
		});

		objekat.html(slova_prikaz);
	}

	set_game();

	$('.let-multi-1, .let-multi-2').click(function(){
		var slovo = $(this);
		var id_game = $("#id_multiplayer_igre").html();
		slovo.parent().html('<img src="../images/end.png" />');
		slovo.hide();
		
			$.post('../ajax-multiplayer-game', {slovo: slovo.attr('href'), id: id_game}, function(data){
				obradi_ajax(data);
				//alert(data);
			});
			
		return false;
	});

	function obradi_ajax(data)
	{
		var id_game = $("#id_multiplayer_igre").html();
		//alert(data);
		var lista = data.split(";");
	    
		var user_id = vrati_vrednost(lista[0]);
		
		var igra_se = vrati_vrednost(lista[1]);
		var pobedio_igrac = vrati_vrednost(lista[2]);
		
		var id_player_1      = vrati_vrednost(lista[3]);
		var data_rec_1       = vrati_vrednost(lista[4]);
		var nepoznata_rec_1  = vrati_vrednost(lista[5]);
		var birana_slova_1   = vrati_vrednost(lista[6]);
		var broj_promasaja_1 = vrati_vrednost(lista[7]);
		var kraj_1           = vrati_vrednost(lista[8]);
		
		var id_player_2      = vrati_vrednost(lista[9]);
		var data_rec_2       = vrati_vrednost(lista[10]);
		var nepoznata_rec_2  = vrati_vrednost(lista[11]);
		var birana_slova_2   = vrati_vrednost(lista[12]);
		var broj_promasaja_2 = vrati_vrednost(lista[13]);
		var kraj_2           = vrati_vrednost(lista[14]);
		
		$('#nepoznata1').html(nepoznata_rec_1);
		$('#birana_slova_1').html(birana_slova_1);
		$('#broj_promasaja_1').html(broj_promasaja_1);
		
		$('#nepoznata2').html(nepoznata_rec_2);
		$('#birana_slova_2').html(birana_slova_2);
		$('#broj_promasaja_2').html(broj_promasaja_2);
		
		
		set_game();
		//alert("igra_se: "+igra_se);
		if(igra_se == 0)
		{			
			if(kraj_1 == 1 && kraj_2 ==0)
				alert("POBEDIO JE PRVI IGRAC!");
			if(kraj_1 == 0 && kraj_2 ==1)
				alert("POBEDIO JE DRUGI IGRAC!");
			if(kraj_1 == kraj_2)
				alert("NERESENO!");
			
			window.location = "../menu";
			
		}		

	}
	
	function vrati_vrednost(str)
	{
		var item = str.split(":");
		return item[1];
	}

	function postavi_lutkicu(broj, igrac)
	{	
		//alert("broj: "+broj+"\n\nigrac: "+igrac+"\n\n"+$('#omca_multi_'+igrac).html());
		
		if(broj == 1)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_1.png');
		if(broj == 2)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_2.png');
		if(broj == 3)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_3.png');
		if(broj == 4)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_4.png');
		if(broj == 5)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_5.png');
		if(broj == 6)
						$('#omca_multi_'+igrac+' img').attr('src','../promasaj_6.png');
	}

	function blokiraj_slova(slova, igrac)
	{					
		slova = slova.trim();
		slova = slova.replace(" ", "")
		slova = slova.replace(",,", ",")
		slova = slova.replace(",,", ",")
		//alert(slova);
		var slovo = slova.split(",");
	
		//$("div#slova_multi_"+igrac).html('Kraj');
		
		
			for(var i = 1; i < slovo.length; i++)
			{
				 $('a.let-multi-'+igrac+'[href="'+slovo[i]+'"]').hide();
				 //$('a.let-multi-'+igrac+'[href="'+slovo[i]+'"]').parent().html('<img src="../images/end.png" />');	
				 
				$("div#slova_multi_"+igrac+" div").each( function(){				
					if ($(this).css('background-image').indexOf("let-"+slovo[i]) >= 0)
						$(this).html('<img src="../images/end.png" />');
				});
			}
		
	}
	
	function update_multi_game()
	{
			var id_game = $("#id_multiplayer_igre").html();
			$.post('../update_multi_game',{id: id_game},function(data){
				obradi_ajax(data);
				//alert(data);
			})
		
	}
	
	
	window.setInterval(update_multi_game, 3000);



/*	
	function postavi_slova(slova)
	{
		var slova_prikaz = "";
		
		$.each(slova, function(index, value) { 			
			var slovo = value;
			if(slovo == "-")
				{
					slova_prikaz += "<div class=\"nepoznato-slovo\"></div>";
				}
			else if(slovo == " ")
				{
					slova_prikaz += "<div class=\"nepoznato-slovo-blanko\"> </div>";
				}
			else
				{
					slova_prikaz += "<div class=\"nepoznato-slovo\">"+value+"</div>";
				}
		});
		
		$('#nepoznato').html(slova_prikaz);
		blokiraj_slova($("#birana_slova").html());
		postavi_lutkicu($("#broj_promasaja").html());
	}
	
	set_game();
	
		
	$('.let').click(function(){
		var slovo = $(this);
		slovo.parent().html('<img src="images/end.png" />');
		slovo.hide();
		
			$.post('/ajax-single-game', {slovo: slovo.attr('href')}, function(data){
				obradi_ajax(data);
				//alert(data);
			});
			
		return false;
	});
	
	function obradi_ajax(data)
	{
		var lista = data.split(";");
		
		var igra_se = vrati_vrednost(lista[0]);
		var broj_promasaja = vrati_vrednost(lista[1]);
		var nepoznata_rec = vrati_vrednost(lista[2]);		
		var birana_slova = vrati_vrednost(lista[3]);
		var pobedio = vrati_vrednost(lista[4]);		

		//alert("igra se: "+igra_se+"\n"+"broj promasaja: "+broj_promasaja+"\n"+"nepoznata rec:"+nepoznata_rec+"\n"+"birana slova: "+birana_slova+"\n"+"pobedio: "+pobedio+"\n");
	
		$("#nepoznata-rec").html(nepoznata_rec);
		$("#broj_promasaja").html(broj_promasaja);
		$("#birana_slova").html(birana_slova);
		$("#igra_se").html(igra_se);
		$("#pobedio").html(pobedio);
		
		postavi_slova(nepoznata_rec);
		postavi_lutkicu(broj_promasaja);
		
		if(igra_se == 0)
		{
			 $('.let').hide();
			 $('.let').parent().html('<img src="images/end.png" />');	
			 if(pobedio == 1)
				alert("KRAJ!\n\nPobedili ste.");
			 else
				alert("KRAJ!\n\nIzgubili ste.");
				
		} 
	}
	
	function postavi_lutkicu(broj)
	{
		if(broj == 1)
						$('#omca img').attr('src','promasaj_1.png');
					if(broj == 2)
						$('#omca img').attr('src','promasaj_2.png');
					if(broj == 3)
						$('#omca img').attr('src','promasaj_3.png');
					if(broj == 4)
						$('#omca img').attr('src','promasaj_4.png');
					if(broj == 5)
						$('#omca img').attr('src','promasaj_5.png');
					if(broj == 6)
						$('#omca img').attr('src','promasaj_6.png');
	}
	
	function blokiraj_slova(slova)
			{	
				//alert("fn blok0");
				slova = slova.trim();
				slova = slova.replace(" ", "")
				slova = slova.replace(",,", ",")
				slova = slova.replace(",,", ",")
				//alert(slova);
				var slovo = slova.split(",");
				
				for(var i = 1; i < slovo.length; i++)
					{
						 $('a[href="'+slovo[i]+'"]').hide();
						 $('a[href="'+slovo[i]+'"]').parent().html('<img src="images/end.png" />');	
					}
			}
	
	function vrati_vrednost(str)
	{
		var item = str.split(":");
		return item[1];
	}
	
	$('.refresh').click(function(){
		window.location.reload();
	});
	*/
	
	
	
	
});















