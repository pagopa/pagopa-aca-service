module.exports = (req, res, next) => {
  const requestPath = req.path.toString();
  console.log(`${new Date().toISOString()} - Received request: ${req.method} ${req.path} - Body:  ${JSON.stringify(req.body)}`);
  const creditorinstitutioncode = [...requestPath.matchAll(/[\d]{11}/g)][0].toString();
  console.log(`Input creditor institution code: ${creditorinstitutioncode}`);
  res.setHeader('X-Request-Id', req.header('X-Request-Id')?req.header('X-Request-Id'):"123");
  switch (creditorinstitutioncode) {
    case "77777777770":
      console.log("Returning 400 error");
      res.sendStatus(400);
      break;
    case "77777777771":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "77777777772":
      console.log("Returning 403 error");
      res.sendStatus(403);
      break;
    case "77777777773":
      console.log("Returning 429 error");
      res.sendStatus(429);
      break;
    case "77777777774":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    case "66666666666":
    case "77777777777":
         req.method = 'GET';
         next();
         break;
     default:
         res.status(500).jsonp({
           "errorMessage": `Mock error, creditorinstitutioncode: ${creditorinstitutioncode} not handled!`
         });
         break;

  }
}