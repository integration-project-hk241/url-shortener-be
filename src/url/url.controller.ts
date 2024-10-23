import { Controller, Get, Post, Body, Param, Res } from '@nestjs/common';
import { UrlService } from './url.service';
import { CreateUrlDto } from './dto/create-url.dto';
import { Response } from 'express';

@Controller()
export class UrlController {
  constructor(private readonly urlService: UrlService) {}

  @Post('shorten')
  shortenUrl(@Body() url: CreateUrlDto) {
    return this.urlService.createShortUrl(url);
  }

  @Get(':code')
  async redirect(@Res() res: Response, @Param('code') code: string) {
    const longUrl = await this.urlService.redirectUrl(code);

    return res.redirect(longUrl);
  }
}
