let testArr = [["A0",
	["B0",
		["C0"], ["C1"]],
	["B1"]],

["A1",
	["B0",
		["C0"]],
	["B1"]]];

function formatReplies(replyArr) {
	if (Array.isArray(replyArr)) {
		for (let i = 0; i < replyArr.length; i++) {
			if(!Array.isArray(replyArr[i])) {
				load(replyArr[i]);
			}
			formatReplies(replyArr[i]);
		}
	}
}

function load(reply) {
	console.log(reply);
}