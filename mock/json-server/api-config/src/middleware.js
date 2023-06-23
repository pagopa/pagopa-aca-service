module.exports = (req, res, next) => {
  console.log(`Received request: ${req.method} - ${JSON.stringify(req.body)}`)
  req.method = 'GET'
  next()
}