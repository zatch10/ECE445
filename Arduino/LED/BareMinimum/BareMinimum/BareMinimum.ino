#define RED 17
#define GREEN 18
#define BLUE 19
#define DELAY  20 

int ctr = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  
}

void loop() {
  // put your main code here, to run repeatedly:
   delay(1000);
   if (ctr == 0){
    digitalWrite(RED,HIGH);
    digitalWrite(GREEN,LOW);
    digitalWrite(BLUE,LOW);
    ctr++;
   }

   else if (ctr == 1){
    digitalWrite(RED,LOW);
    digitalWrite(GREEN,HIGH);
    digitalWrite(BLUE,LOW);
    ctr++;
   }

   else{
    digitalWrite(RED,LOW);
    digitalWrite(GREEN,LOW);
    digitalWrite(BLUE,HIGH);
    ctr = 0;
    delay(1000);
    }
}
