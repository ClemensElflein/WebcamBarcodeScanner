# README

This tool started out as a simple generic barcode scanner but evolved to a more complex solution to add LCSC ordered parts to the PartsBox web interface.

The reason for this is, that PartsBox does not find the parts from LCSC's QR codes.

This app does the following:
- Wait for an LCSC QR code in the webcam view.
- Scan the code and parse the LCSC website for part info
- Fill the "Create Local Part" of the PartsBox.com interface by pasting the info retrieved from LCSC.com


## Hints
Don't expect this to work instantly - LCSC might change their QR codes or their website. Also the PartsBox web interface could change without notice.
