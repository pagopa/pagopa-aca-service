module.exports = (req, res, next) => {
  //ibans-enhanced-:creditorinstitutioncode-:label  77777777777,7
  const requestPath = req.path.toString();
  console.log(`${new Date().toISOString()} - Received request: ${req.method} ${req.path} - Body:  ${JSON.stringify(req.body)}`);
  const creditorinstitutioncode = [...requestPath.matchAll(/[\d]{11}/g)][0].toString();
  console.log(`Input creditor institution code: ${creditorinstitutioncode}`);
  res.setHeader('X-Request-Id', req.header('X-Request-Id')?req.header('X-Request-Id'):"123");
  switch (creditorinstitutioncode) {
    case "00000000000":
      console.log("Returning 400 error");
      res.sendStatus(400);
      break;
    case "11111111111":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "22222222222":
      console.log("Returning 403 error");
      res.sendStatus(403);
      break;
    case "33333333333":
      console.log("Returning 429 error");
      res.sendStatus(429);
      break;
    case "44444444444":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    default:
      req.method = 'GET';
      next();
      break;

  }
}