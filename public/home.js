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
	let posts = "";
	fetch(url)
		.then((resp) => resp.json)
		.then(function (data) {
			posts = data;
		})
		.catch(function (error) {
			console.log(error);
		});

		console.log(posts);
}

/*
var request = new Request(url, {
    method: 'POST',
    body: data,
    headers: new Headers()
});
*/