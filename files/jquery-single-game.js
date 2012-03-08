$(document).ready(function(){
				
	function set_game()
	{
		var rec = $('#nepoznata-rec').html();			
		var slova = rec.split("");
		
		postavi_slova(slova);
	}
	
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
		
			$.post('ajax-single-game', {slovo: slovo.attr('href')}, function(data){
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
	
});















