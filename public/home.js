let testArr = [["A0",
	["B0",
		["C0"], ["C1"]],
	["B1"]],

["A1",
	["B0",
		["C0"]],
	["B1"]]];

let num = -2;
let display = "";

function formatReplies(replyArr) {
	num++;
	if (Array.isArray(replyArr)) {
		for (let i = 0; i < replyArr.length; i++) {
			if (!Array.isArray(replyArr[i])) {
				load(replyArr[i]);
			}
			formatReplies(replyArr[i]);
			num--;
		}
	}
}

function load(reply) {
	for (let i = 0; i < num; i++) {
		reply = "    " + reply;
	}
	display += (reply + "<br>");
}

formatReplies(testArr);
console.log(display);


function getPosts(url) {
	fetch(url)
		.then((resp) => resp.json)
		.then(function (data) {
			let posts = data.results;
		})
		.catch(function (error) {
			console.log(error);
		});
}