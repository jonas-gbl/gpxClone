var temp=2;
$(document).ready
(
	function() 
	{
		temp=3;
		var validator = $("#memberForm").validate({
			rules:{
				"user.username": {
					required: true,
					minlength: 2,
				},
				"user.email": {
					required: true,
					email: true
				},
				"user.password": {
					required: true,
					minlength: 5
					
				},
				"passwordConfirm": {
					required: true,
					minlength: 5,
					equalTo: "#passwd"
				},
				"user.passwordQuestion": {
					required: true,
					minlength: 3
				},
				"user.passwordAnswer": {
					required: true,
					minlength: 3
				}
			},
			messages: {
				"user.username": {
					required: "Enter a username",
					minlength: jQuery.format("Username must be at least {0} characters")
				},
				"user.email": {
					required: "Please enter a valid email address, now!",
					email: "Please enter a valid email address, come on!"
				},
				"user.password": {
					required: "Provide a password",
					minlength: jQuery.format("Enter at least {0} characters")
				},
				"passwordConfirm": {
					required: "Repeat your password",
					minlength: jQuery.format("Enter at least {0} characters"),
					equalTo: "Password fields don't match"
				},
				"user.passwordQuestion": {
					required: "Provide a Security Question",
					minlength: jQuery.format("Enter at least {0} characters"),
				},
				"user.passwordAnswer": {
					required: "Provide a Security Answer",
					minlength: jQuery.format("Enter at least {0} characters"),
				}
			},
			errorElement: "div"
		});
	}
);

	