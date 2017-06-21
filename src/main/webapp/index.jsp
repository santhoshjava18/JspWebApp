<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Blunderful Online Oppurtunities Management, Inc</title>
</head>
<body>

<h1>Welcome to Blunderful Online Oppurtunities Management, Inc!</h1>
<p>Blunderful Online Oppurtunities Management, Inc, or "B.O.O.M." is dedicated to finding
and hiring top-notch, elite software developers in the Java industry.</p>
<p>Be part of our team! Click <a href="register.jsp">here</a> to register.</p>

<p>Already registered? Sign in below.</p>

<form action="account">
	<input type="hidden" name="action" value="login"/>
	<input type="hidden" name="successUrl" value="account?action=view"/>
	<table>
		<tr>
			<td>Username:</td>
			<td><input type="text" name="username"/></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="password"/></td>
		</tr>
		<tr>
			<td colspan="2">
				<input type="submit" name="Login" value="Login"/>
			</td>
		</tr>
	</table>
</form>

</body>
</html>