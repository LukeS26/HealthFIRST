function getChallenges(page) {
	fetch(`http://157.230.233.218:8080/api/challenges/feed?page=${page}`)
		.then(res => res.json()).then(function (json) {
			for (let i = 0; i < json["feed"].length; i++) {
				displayChallenge(json["feed"][i]["title"], json["feed"][i]["body"], json["feed"][i]["end_date"]["$date"], json["feed"][i]["challenge_id"]);
			}
		});

		if (document.getElementById("loadingChallenges")) {
			document.getElementById("loadingChallenges").remove();
		}
}

function displayChallenge(title, body, date, id) {
	let dateFormatted = new Date(parseInt(date)).toLocaleString();
	let shell = document.getElementById("challenges");
	let html = "";
	html += `<h3> ${title} </h3>`;
	html += `<p> Ends ${dateFormatted} </p>`
	html += `<p> ${body} </p>`;

	if (new Date(parseInt(date)).getTime() < new Date().getTime()) {
		//grey out the box, and make the button not work
		html += `<button class="challengeButtonFinished"> Challenge Ended </button>`

		shell.innerHTML += `<div class="challengeContainerFinished"> ${html} </div>`
	} else {
		html += `<button class="challengeButton" onClick="finishChallenge('${id}')"> Complete Challenge </button>`

		shell.innerHTML += `<div class="challengeContainer"> ${html} </div>`
	}
}

function finishChallenge(id) {
	fetch(`http://157.230.233.218:8080/api/challenges/complete/${id}`, {
		method: "POST",
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://healthfirst4342.tk/"
		}
	});
}

let page = 0;

getChallenges(page);