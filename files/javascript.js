	$(document).ready(function(){
						
			$('#menu_up').mouseover(function(){				
				$('#menu_bg1').show();
				$('#menu_bg2').hide();
				$('#menu_bg3').hide();
				
			});	
			$('#menu_midd').mouseover(function(){				
				$('#menu_bg1').hide();
				$('#menu_bg2').show();
				$('#menu_bg3').hide();
			});	
			$('#menu_down').mouseover(function(){				
				$('#menu_bg1').hide();
				$('#menu_bg2').hide();
				$('#menu_bg3').show();
			});	
		
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
			$.post('ajax-log-refresh', function(data){
				//alert(data)				
			});
		}
			
		window.setInterval(update_log_time, 5000);
		
		

});
	