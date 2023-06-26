module.exports = (req, res, next) => {
  const requestPath = req.path;
  console.log(`${new Date().toISOString()} - Received request: ${req.method} ${requestPath} - Body:  ${JSON.stringify(req.body)}`);
  //organizationfiscalcode
  const organizationFiscalCode = [...requestPath.matchAll(/[\d]{11}/g)][0].toString();
  console.log(`Input organization fiscal code: ${organizationFiscalCode}`);
  if (req.url.startsWith("/debit-position-invalidate-")) {
    handleInvalidateDebtPositionResponse(req, res, next, organizationFiscalCode);
  } else {
    switch (req.method) {
      case "GET":
        handleGetDebtPositionResponse(req, res, next, organizationFiscalCode);
        break;
      case "POST":
        handleCreateDebtPositionResponse(req, res, next, organizationFiscalCode);
        break;
      case "PUT":
        handleUpdateDebtPositionResponse(req, res, next, organizationFiscalCode);
        break;
    }
  }
}

function handleGetDebtPositionResponse(req, res, next, organizationFiscalCode) {
  console.log("Handling get debt position");
  switch (organizationFiscalCode) {
    case "77777777770":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "77777777771":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    case "66666666666":
      console.log("Returning 404 not found (creation flow)");
      res.sendStatus(404);
      break;
    case "77777777777":
      req.method = 'GET';
      next();
      break;
    default:
      res.status(500).jsonp({
        "errorMessage": `Mock error, organizationFiscalCode: ${organizationFiscalCode} not handled!`
      });
      break;
  }
}


function handleCreateDebtPositionResponse(req, res, next, organizationFiscalCode) {
  console.log("Handling create debt position");
  switch (organizationFiscalCode) {
    case "77777777770":
      console.log("Returning 400 error");
      res.sendStatus(400);
      break;
    case "77777777771":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "77777777772":
      console.log("Returning 409 error");
      res.sendStatus(409);
      break;
    case "77777777773":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    case "77777777777":
    case "66666666666":
      req.method = 'GET';
      next();
      res.status(201);
      break;
    default:
      res.status(500).jsonp({
        "errorMessage": `Mock error, organizationFiscalCode: ${organizationFiscalCode} not handled!`
      });
      break;
  }
}

function handleUpdateDebtPositionResponse(req, res, next, organizationFiscalCode) {
  console.log("Handling update debt position");
  switch (organizationFiscalCode) {
    case "77777777770":
      console.log("Returning 400 error");
      res.sendStatus(400);
      break;
    case "77777777771":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "77777777772":
      console.log("Returning 409 error");
      res.sendStatus(409);
      break;
    case "77777777773":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    case "77777777777":
      req.method = 'GET';
      next();
      break;
    default:
      res.status(500).jsonp({
        "errorMessage": `Mock error, organizationFiscalCode: ${organizationFiscalCode} not handled!`
      });
      break;
  }
}

function handleInvalidateDebtPositionResponse(req, res, next, organizationFiscalCode) {
  console.log("Handling invalidate debt position");
  switch (organizationFiscalCode) {
    case "77777777770":
      console.log("Returning 401 error");
      res.sendStatus(401);
      break;
    case "77777777771":
      console.log("Returning 404 error");
      res.sendStatus(404);
      break;
    case "77777777772":
      console.log("Returning 409 error");
      res.sendStatus(409);
      break;
    case "77777777773":
      console.log("Returning 500 error");
      res.status(500).jsonp({
        "detail": "There was an error processing the request",
        "status": 500,
        "title": "Internal error"
      });
      break;
    case "77777777777":
      req.method = 'GET';
      next();
      break;
    default:
      res.status(500).jsonp({
        "errorMessage": `Mock error, organizationFiscalCode: ${organizationFiscalCode} not handled!`
      });
      break;
  }
}