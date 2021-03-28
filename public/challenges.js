function getChallenges(page) {
	fetch(`http://157.230.233.218:8080/api/challenges/feed?page=${page}`)
	.then(res => res.json()).then(function(json) {
		for(let i = 0; i < json["feed"].length; i++) {
			displayChallenge(json["feed"][i]["title"], json["feed"][i]["body"], json["feed"][i]["end_date"]);
		}
	});
}

function displayChallenge(title, body, date) {
	let dateFormatted = new Date(date).toLocaleString();
	let shell = document.getElementById("challenges");
	let html = "";
	
	html += `<h3> ${title} </h3>`;
	html += `<p> End: ${dateFormatted} </p>`
	html += `<p> ${body} </p>`;

	shell.innerHTML += `<div class="postContainer"> ${html} </div>`
}

let page = 0;

getChallenges(page);