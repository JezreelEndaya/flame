import { Controller, Get, Param } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Get("assignment-1-fibonacci-sequence/:terms")
  getFibonacciSequence(@Param("terms") terms: string): { sequence: number[] } {
    const numberOfTerms = parseInt(terms, 10); 
    const sequence = [0, 1];

    for (let i = 2; i < numberOfTerms; i++) {
      sequence.push(sequence[i - 1] + sequence[i - 2]);
    }

    return { 
      "sequence" : sequence 
    };
  }

  @Get("assignment-2-prime-number/:number")
  getPrimeNumber(@Param('number') number: string): { isPrime: boolean } {
    const num = parseInt(number);

    if (isNaN(num)) {
      return { isPrime: false };
    }

    if (num <= 1) {
      return { isPrime: false };
    }

    for (let i = 2; i < Math.floor(num); i++) {
      if (num % i === 0) {
        return { isPrime: false };
      }
    }

    return { 
      "isPrime" : true 
    };
  }
}
