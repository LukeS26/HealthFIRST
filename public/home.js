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
				load(replyArr[i], num);
			}
			formatReplies(replyArr[i]);
			num--;
		}
	}
}

function load(reply, number) {
	for (let i = 0; i < number; i++) {
		reply = "    " + reply;
	}
	display += (reply + "\n");
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

/*
var request = new Request(url, {
    method: 'POST',
    body: data,
    headers: new Headers()
});
*/